package xyz.jienan.xkcd.whatif.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;
import xyz.jienan.xkcd.BuildConfig;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.base.BaseFragment;
import xyz.jienan.xkcd.model.WhatIfModel;
import xyz.jienan.xkcd.model.persist.SharedPrefManager;
import xyz.jienan.xkcd.ui.WhatIfWebView;
import xyz.jienan.xkcd.whatif.interfaces.ImgInterface;
import xyz.jienan.xkcd.whatif.interfaces.LatexInterface;
import xyz.jienan.xkcd.whatif.interfaces.RefInterface;

import static android.view.HapticFeedbackConstants.CONTEXT_CLICK;
import static android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
import static android.view.HapticFeedbackConstants.LONG_PRESS;
import static xyz.jienan.xkcd.Const.FIRE_GO_WHAT_IF_MENU;
import static xyz.jienan.xkcd.Const.FIRE_SHARE_BAR;
import static xyz.jienan.xkcd.Const.FIRE_WHAT_IF_IMG_LONG;
import static xyz.jienan.xkcd.Const.FIRE_WHAT_IF_REF;
import static xyz.jienan.xkcd.Const.FIRE_WHAT_IF_SUFFIX;

/**
 * Created by jienanzhang on 03/03/2018.
 */

public class SingleWhatIfFragment extends BaseFragment implements ImgInterface.ImgCallback, RefInterface.RefCallback {

    @BindView(R.id.webview_what_if)
    public WhatIfWebView webView;

    private int id = -1;

    private WhatIfMainFragment parentFragment;

    private LatexInterface latexInterface = new LatexInterface();

    protected CompositeDisposable compositeDisposable = new CompositeDisposable();

    private AlertDialog dialog = null;

    protected SharedPrefManager sharedPref = new SharedPrefManager();

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
            id = args.getInt("id", -1);
        setHasOptionsMenu(true);
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
        webView.getSettings().setTextZoom(sharedPref.getWhatIfZoom());
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        }
        if (id != -1) {
            final WhatIfModel model = WhatIfModel.getInstance();

            compositeDisposable.add(model.loadArticle(id)
                    .doOnSuccess(model::push)
                    .subscribe(article -> {
                                if (webView != null) {
                                    webView.loadDataWithBaseURL("file:///android_asset/",
                                            article.content.replaceAll("\\$", "&#36;"), "text/html", "UTF-8", null);
                                }
                            },
                            Timber::e));

            webView.setCallback(new WebViewScrollCallback(this));
            webView.setLatexScrollInterface(latexInterface);
            webView.addJavascriptInterface(latexInterface, "AndroidLatex");
            webView.addJavascriptInterface(new ImgInterface(this), "AndroidImg");
            webView.addJavascriptInterface(new RefInterface(this), "AndroidRef");

            if (getParentFragment() instanceof WhatIfMainFragment) {
                parentFragment = ((WhatIfMainFragment) getParentFragment());
            }
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        }
        setHasOptionsMenu(true);
        compositeDisposable.add(WhatIfModel
                .getInstance()
                .observeZoom()
                .subscribe(zoom -> webView.getSettings().setTextZoom(zoom),
                        e -> Timber.e(e, "observing zoom error")));
        return view;
    }

    @Override
    public void onDestroyView() {
        if (dialog != null) {
            dialog.dismiss();
        }
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
        if (webView != null) {
            Timber.d("OkHttp: clear %d", id);
            webView.clearTasks();
        }
        super.onDestroyView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text_what_if, "https://whatif.xkcd.com/" + id));
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_to)));
                logUXEvent(FIRE_SHARE_BAR + FIRE_WHAT_IF_SUFFIX);
                return true;
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

    private void scrolledToTheEnd(boolean isTheEnd) {
        if (parentFragment != null) {
            if (!parentFragment.fab.isShown() && isTheEnd) {
                parentFragment.showOrHideFabWithInfo(true);
            } else if (parentFragment.fab.isShown() && !isTheEnd) {
                parentFragment.showOrHideFabWithInfo(false);
            }
        }
    }

    @Override
    public void onImgLongClick(String title) {
        compositeDisposable.add(Observable.just(title)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showSimpleInfoDialog,
                        e -> Timber.e("long click error")));
        if (this.getView() != null) {
            this.getView().performHapticFeedback(LONG_PRESS, FLAG_IGNORE_GLOBAL_SETTING);
        }
        logUXEvent(FIRE_WHAT_IF_IMG_LONG);
    }

    @Override
    public void onRefClick(String content) {
        compositeDisposable.add(Observable.just(content)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showSimpleInfoDialog,
                        e -> Timber.e(e, "ref click error")));
        if (this.getView() != null) {
            this.getView().performHapticFeedback(CONTEXT_CLICK, FLAG_IGNORE_GLOBAL_SETTING);
        }
        logUXEvent(FIRE_WHAT_IF_REF);
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

    private class WebViewScrollCallback implements WhatIfWebView.ScrollToEndCallback {

        private WeakReference<SingleWhatIfFragment> weakReference;

        WebViewScrollCallback(SingleWhatIfFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void scrolledToTheEnd(boolean isTheEnd) {
            if (weakReference.get() != null) {
                SingleWhatIfFragment fragment = weakReference.get();
                fragment.scrolledToTheEnd(isTheEnd);
            }
        }
    }
}
