package xyz.jienan.xkcd.whatif.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;

import butterknife.BindView;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import timber.log.Timber;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.base.BaseFragment;
import xyz.jienan.xkcd.comics.dialog.SimpleInfoDialogFragment;
import xyz.jienan.xkcd.model.WhatIfModel;
import xyz.jienan.xkcd.ui.WhatIfWebView;
import xyz.jienan.xkcd.whatif.ImgInterface;
import xyz.jienan.xkcd.whatif.LatexInterface;
import xyz.jienan.xkcd.whatif.RefInterface;

import static android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
import static android.view.HapticFeedbackConstants.LONG_PRESS;
import static xyz.jienan.xkcd.Const.FIRE_LONG_PRESS;

/**
 * Created by jienanzhang on 03/03/2018.
 */

public class SingleWhatIfFragment extends BaseFragment implements WhatIfWebView.ScrollToEndCallback, ImgInterface.ImgCallback {

    @BindView(R.id.webview_what_if)
    WhatIfWebView webView;

    private int id;

    private WhatIfMainFragment parentFragment;

    private LatexInterface latexInterface = new LatexInterface();

    private Disposable disposable = Disposables.disposed();

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
        disposable = model.loadArticle(id)
                .doOnSuccess(model::push)
                .subscribe(article -> {
                            if (webView != null) {
                                webView.loadDataWithBaseURL("file:///android_asset/", article.content, "text/html", "UTF-8", null);
                            }
                        },
                        Timber::e);
        webView.setCallback(this);
        webView.setLatexScrollInterface(latexInterface);
        webView.addJavascriptInterface(latexInterface, "AndroidLatex");
        webView.addJavascriptInterface(new ImgInterface(this), "AndroidImg");
        webView.addJavascriptInterface(new RefInterface(), "ref");
        parentFragment = ((WhatIfMainFragment) getParentFragment());
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
        disposable.dispose();
        super.onDestroyView();
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
        AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
        dialog.setMessage(title);
        dialog.show();
        this.getView().performHapticFeedback(LONG_PRESS, FLAG_IGNORE_GLOBAL_SETTING);
        logUXEvent(FIRE_LONG_PRESS);
    }
}
