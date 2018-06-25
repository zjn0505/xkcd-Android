package xyz.jienan.xkcd.whatif.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.base.BaseFragment;
import xyz.jienan.xkcd.list.activity.WhatIfListActivity;
import xyz.jienan.xkcd.model.WhatIfModel;
import xyz.jienan.xkcd.ui.WhatIfWebView;
import xyz.jienan.xkcd.whatif.interfaces.ImgInterface;
import xyz.jienan.xkcd.whatif.interfaces.LatexInterface;
import xyz.jienan.xkcd.whatif.interfaces.RefInterface;

import static android.view.HapticFeedbackConstants.CONTEXT_CLICK;
import static android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
import static android.view.HapticFeedbackConstants.LONG_PRESS;
import static xyz.jienan.xkcd.Const.FIRE_BROWSE_LIST_MENU;
import static xyz.jienan.xkcd.Const.FIRE_GO_WHAT_IF_MENU;

/**
 * Created by jienanzhang on 03/03/2018.
 */

public class SingleWhatIfFragment extends BaseFragment implements WhatIfWebView.ScrollToEndCallback, ImgInterface.ImgCallback, RefInterface.RefCallback {

    @BindView(R.id.webview_what_if)
    WhatIfWebView webView;

    private int id;

    private WhatIfMainFragment parentFragment;

    private LatexInterface latexInterface = new LatexInterface();

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private AlertDialog dialog = null;

    public static SingleWhatIfFragment newInstance(int articleId) {
        SingleWhatIfFragment fragment = new SingleWhatIfFragment();
        Bundle args = new Bundle();
        args.putInt("id", articleId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_what_if_single;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null)
            id = args.getInt("id");
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setUseWideViewPort(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        final WhatIfModel model = WhatIfModel.getInstance();
        compositeDisposable.add(model.loadArticle(id)
                .doOnSuccess(model::push)
                .subscribe(article -> {
                            if (webView != null) {
                                webView.loadDataWithBaseURL("file:///android_asset/", article.content, "text/html", "UTF-8", null);
                            }
                        },
                        Timber::e));
        webView.setCallback(this);
        webView.setLatexScrollInterface(latexInterface);
        webView.addJavascriptInterface(latexInterface, "AndroidLatex");
        webView.addJavascriptInterface(new ImgInterface(this), "AndroidImg");
        webView.addJavascriptInterface(new RefInterface(this), "AndroidRef");
        parentFragment = ((WhatIfMainFragment) getParentFragment());
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void scrolledToTheEnd(boolean isTheEnd) {
        if (parentFragment != null) {
            if (!parentFragment.fab.isShown() && isTheEnd) {
                parentFragment.showOrHideFabWithInfo(true);
            } else if (parentFragment.fab.isShown() && !isTheEnd) {
                parentFragment.showOrHideFabWithInfo(false);
            }
        }
    }

    @Override
    public void onDestroyView() {
        if (dialog != null) {
            dialog.dismiss();
        }
        compositeDisposable.dispose();
        super.onDestroyView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_go_xkcd:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://whatif.xkcd.com/" + id));
                startActivity(browserIntent);
                logUXEvent(FIRE_GO_WHAT_IF_MENU);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateFab() {
        if (webView.distanceToEnd() < 200) {
            scrolledToTheEnd(true);
        } else if (webView.distanceToEnd() > 250) {
            scrolledToTheEnd(false);
        }
    }

    @Override
    public void onImgLongClick(String title) {
        compositeDisposable.add(Observable.just(title).observeOn(AndroidSchedulers.mainThread()).subscribe(this::showSimpleInfoDialog));
        if (this.getView() != null) {
            this.getView().performHapticFeedback(LONG_PRESS, FLAG_IGNORE_GLOBAL_SETTING);
        }
    }

    @Override
    public void onRefClick(String content) {
        compositeDisposable.add(Observable.just(content).observeOn(AndroidSchedulers.mainThread()).subscribe(this::showSimpleInfoDialog));
        if (this.getView() != null) {
            this.getView().performHapticFeedback(CONTEXT_CLICK, FLAG_IGNORE_GLOBAL_SETTING);
        }
    }

    private void showSimpleInfoDialog(String content) {
        dialog = new AlertDialog.Builder(getContext()).create();
        LinearLayout view = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_explain, null);
        TextView textView = view.findViewById(R.id.tv_explain);
        Document document = Jsoup.parse(content);
        Elements elements = document.select("img.illustration");
        for (Element element : elements) {
            element.remove();
            ImageView iv = new ImageView(getContext());
            Glide.with(getContext()).load(element.absUrl("src")).fitCenter().into(iv);
            view.addView(iv);
        }
        textView.setText(Html.fromHtml(document.html()));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        dialog.setView(view);
        dialog.show();
    }
}
