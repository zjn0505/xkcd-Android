package xyz.jienan.xkcd.extra.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindDimen;
import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.footer.ClassicFooter;
import me.dkzwm.widget.srl.extra.header.ClassicHeader;
import timber.log.Timber;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.model.ExtraComics;
import xyz.jienan.xkcd.model.ExtraModel;
import xyz.jienan.xkcd.ui.RefreshFooterView;
import xyz.jienan.xkcd.ui.RefreshHeaderView;
import xyz.jienan.xkcd.whatif.fragment.SingleWhatIfFragment;

import static xyz.jienan.xkcd.Const.FIRE_EXTRA_SUFFIX;
import static xyz.jienan.xkcd.Const.FIRE_GO_EXTRA_MENU;
import static xyz.jienan.xkcd.Const.FIRE_SHARE_BAR;

/**
 * Created by jienanzhang on 03/03/2018.
 */

public class SingleExtraWebViewFragment extends SingleWhatIfFragment {

    @BindView(R.id.refresh_layout)
    SmoothRefreshLayout mRefreshLayout;

    private ExtraComics extraComics;

    private int currentPage = 0;

    @BindDimen(R.dimen.refresh_text_size)
    float refreshTextSize;

    public static SingleExtraWebViewFragment newInstance(ExtraComics extraComics) {
        SingleExtraWebViewFragment fragment = new SingleExtraWebViewFragment();
        Bundle args = new Bundle();
        args.putSerializable("entity", extraComics);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_extra_single;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null)
            extraComics = (ExtraComics) args.getSerializable("entity");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webView.getSettings().setTextZoom((int) (sharedPref.getWhatIfZoom() * 1.5));
        if (savedInstanceState != null) {
            currentPage = savedInstanceState.getInt("current", 0);
        }
        loadLinkPage(currentPage);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (extraComics.links.size() > 1) {

            mRefreshLayout.setDisableLoadMore(false);
            mRefreshLayout.setDisablePerformRefresh(false);
            mRefreshLayout.setDisablePerformLoadMore(false);
            mRefreshLayout.setEnableKeepRefreshView(false);

            mRefreshLayout.setOnRefreshListener(new SmoothRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefreshing() {
                    Timber.d("Refresh");
                    loadLinkPage(--currentPage);
                    mRefreshLayout.refreshComplete();
                    updateReleaseText();
                }

                @Override
                public void onLoadingMore() {
                    Timber.d("LoadMore");
                    loadLinkPage(++currentPage);
                    mRefreshLayout.refreshComplete();
                    Observable.timer(700, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnNext(ignored -> webView.scrollTo(0, 0))
                            .subscribe();
                    updateReleaseText();
                }
            });
            mRefreshLayout.setEnableAutoRefresh(true);
            mRefreshLayout.setEnableAutoLoadMore(true);
            RefreshHeaderView header = new RefreshHeaderView(getContext());
            header.setTextSize(refreshTextSize);
            mRefreshLayout.setHeaderView(header);
            RefreshFooterView footer = new RefreshFooterView(getContext());
            footer.setTextSize(refreshTextSize);
            mRefreshLayout.setFooterView(footer);
            updateReleaseText();
        }
    }

    @Override
    public void onDestroyView() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
        if (webView != null) {
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
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text_article_extra, extraComics.links.get(0)));
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_to)));
                logUXEvent(FIRE_SHARE_BAR + FIRE_EXTRA_SUFFIX);
                return true;
            case R.id.action_go_explain: {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(extraComics.explainUrl));
                startActivity(browserIntent);
                logUXEvent(FIRE_GO_EXTRA_MENU);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current", currentPage);
    }

    private void loadLinkPage(int pageIndex) {

        List<String> links = extraComics.links;

        compositeDisposable.add(
                ExtraModel.getInstance().parseContentFromUrl(links.get(Math.abs(pageIndex % links.size())))
                        .subscribe(html -> webView.loadDataWithBaseURL("file:///android_asset/.",
                                html, "text/html", "UTF-8", null),
                                Timber::e));
    }

    private void updateReleaseText() {
        if (extraComics.num == 1) {
            if (Math.abs(currentPage) % 2 != 0) {
                ((ClassicHeader) mRefreshLayout.getHeaderView()).setReleaseToRefreshRes(R.string.release_for_puzzle);
                ((ClassicFooter) mRefreshLayout.getFooterView()).setReleaseToLoadRes(R.string.release_for_puzzle);
            } else {
                ((ClassicHeader) mRefreshLayout.getHeaderView()).setReleaseToRefreshRes(R.string.release_for_solution);
                ((ClassicFooter) mRefreshLayout.getFooterView()).setReleaseToLoadRes(R.string.release_for_solution);
            }
        }
    }
}
