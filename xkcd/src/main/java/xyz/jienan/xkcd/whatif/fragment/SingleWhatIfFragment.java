package xyz.jienan.xkcd.whatif.fragment;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;

import java.util.List;
import java.util.Observable;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnLongClick;
import timber.log.Timber;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.model.WhatIfArticle;
import xyz.jienan.xkcd.model.WhatIfModel;
import xyz.jienan.xkcd.model.XkcdPic;
import xyz.jienan.xkcd.base.BaseFragment;
import xyz.jienan.xkcd.base.glide.ProgressTarget;
import xyz.jienan.xkcd.comics.SearchCursorAdapter;
import xyz.jienan.xkcd.comics.activity.ImageDetailPageActivity;
import xyz.jienan.xkcd.comics.contract.SingleComicContract;
import xyz.jienan.xkcd.comics.dialog.SimpleInfoDialogFragment;
import xyz.jienan.xkcd.comics.dialog.SimpleInfoDialogFragment.ISimpleInfoDialogListener;
import xyz.jienan.xkcd.comics.presenter.SingleComicPresenter;
import xyz.jienan.xkcd.home.MainActivity;

import static android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
import static android.view.HapticFeedbackConstants.LONG_PRESS;
import static xyz.jienan.xkcd.Const.FIRE_GO_EXPLAIN_MENU;
import static xyz.jienan.xkcd.Const.FIRE_GO_XKCD_MENU;
import static xyz.jienan.xkcd.Const.FIRE_LONG_PRESS;
import static xyz.jienan.xkcd.Const.FIRE_MORE_EXPLAIN;
import static xyz.jienan.xkcd.Const.FIRE_SHARE_BAR;
import static xyz.jienan.xkcd.Const.XKCD_INDEX_ON_NEW_INTENT;

/**
 * Created by jienanzhang on 03/03/2018.
 */

public class SingleWhatIfFragment extends BaseFragment {

    @BindView(R.id.webview_what_if)
    WebView webView;

    private SimpleInfoDialogFragment dialogFragment;

    private int id;

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
        WhatIfModel.getInstance()
                .loadArticle(id)
                .subscribe(articleHtml -> {
                            if (webView != null) {
                                webView.loadDataWithBaseURL("file:///android_asset/", articleHtml, "text/html", "UTF-8", null);
                            }
                        },
                        e -> Timber.e(e)
                );
        return view;
    }

}
