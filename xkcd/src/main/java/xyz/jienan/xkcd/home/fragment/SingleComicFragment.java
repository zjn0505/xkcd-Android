package xyz.jienan.xkcd.home.fragment;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnLongClick;
import butterknife.Unbinder;
import timber.log.Timber;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.XkcdPic;
import xyz.jienan.xkcd.base.glide.ProgressTarget;
import xyz.jienan.xkcd.home.SearchCursorAdapter;
import xyz.jienan.xkcd.home.activity.ImageDetailPageActivity;
import xyz.jienan.xkcd.home.activity.MainActivity;
import xyz.jienan.xkcd.home.contract.SingleComicContract;
import xyz.jienan.xkcd.home.dialog.SimpleInfoDialogFragment;
import xyz.jienan.xkcd.home.dialog.SimpleInfoDialogFragment.ISimpleInfoDialogListener;
import xyz.jienan.xkcd.home.presenter.SingleComicPresenter;

import static android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
import static android.view.HapticFeedbackConstants.LONG_PRESS;
import static xyz.jienan.xkcd.Const.FIRE_GO_EXPLAIN_MENU;
import static xyz.jienan.xkcd.Const.FIRE_GO_XKCD_MENU;
import static xyz.jienan.xkcd.Const.FIRE_LONG_PRESS;
import static xyz.jienan.xkcd.Const.FIRE_MORE_EXPLAIN;
import static xyz.jienan.xkcd.Const.FIRE_SHARE_BAR;
import static xyz.jienan.xkcd.Const.FIRE_UX_ACTION;
import static xyz.jienan.xkcd.Const.XKCD_INDEX_ON_NEW_INTENT;

/**
 * Created by jienanzhang on 03/03/2018.
 */

public class SingleComicFragment extends Fragment implements SingleComicContract.View {

    @BindView(R.id.tv_title)
    TextView tvTitle;

    @BindView(R.id.iv_xkcd_pic)
    ImageView ivXkcdPic;

    @BindView(R.id.tv_create_date)
    TextView tvCreateDate;

    @Nullable
    @BindView(R.id.tv_description)
    TextView tvDescription;

    @BindView(R.id.pb_loading)
    ProgressBar pbLoading;

    @BindView(R.id.btn_reload)
    Button btnReload;

    @BindString(R.string.search_hint)
    String searchHint;

    private SimpleInfoDialogFragment dialogFragment;

    private FirebaseAnalytics mFirebaseAnalytics;

    private int id;

    private XkcdPic currentPic;

    private Unbinder unbinder;

    private RequestListener<String, Bitmap> glideListener = new RequestListener<String, Bitmap>() {
        @Override
        public boolean onException(Exception e, final String model, final Target<Bitmap> target, boolean isFirstResource) {
            if (btnReload == null) {
                return false;
            }
            btnReload.setVisibility(View.VISIBLE);
            btnReload.setOnClickListener(view -> {
                pbLoading.clearAnimation();
                pbLoading.setAnimation(AnimationUtils.loadAnimation(pbLoading.getContext(), R.anim.rotate));
                Glide.with(getActivity()).load(currentPic.getImg()).asBitmap().fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE).listener(glideListener).into(target);
                btnReload.setVisibility(View.GONE);
            });
            return false;
        }

        @Override
        public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
            if (ivXkcdPic != null) {
                ivXkcdPic.setOnClickListener(v -> launchDetailPageActivity());
            }
            singleComicPresenter.updateXkcdSize(currentPic, resource);
            return false;
        }
    };

    private ProgressTarget<String, Bitmap> target;

    private List<XkcdPic> searchSuggestions;

    private SearchCursorAdapter searchAdapter;

    private ISimpleInfoDialogListener dialogListener = new ISimpleInfoDialogListener() {
        @Override
        public void onPositiveClick() {
            // Do nothing
        }

        @Override
        public void onNegativeClick() {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.explainxkcd.com/wiki/index.php/" + currentPic.num));
            if (browserIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(browserIntent);
            }
        }

        @SuppressLint({"StaticFieldLeak", "CheckResult"})
        @Override
        public void onExplainMoreClick(final SimpleInfoDialogFragment.ExplainingCallback explainingCallback) {
            SingleComicFragment.this.explainingCallback = explainingCallback;
            singleComicPresenter.getExplain(currentPic.num);
            logUXEvent(FIRE_MORE_EXPLAIN);
        }
    };

    private SimpleInfoDialogFragment.ExplainingCallback explainingCallback;

    public void explainLoaded(String result) {
        if (!TextUtils.isEmpty(result)) {
            explainingCallback.explanationLoaded(result);
        } else {
            explainingCallback.explanationFailed();
        }
    }

    public void explainFailed() {
        explainingCallback.explanationFailed();
    }

    private SingleComicContract.Presenter singleComicPresenter;

    public static SingleComicFragment newInstance(int comicId) {
        SingleComicFragment fragment = new SingleComicFragment();
        Bundle args = new Bundle();
        args.putInt("id", comicId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        singleComicPresenter = new SingleComicPresenter(this);
        Bundle args = getArguments();
        if (args != null)
            id = args.getInt("id");
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comic, container, false);
        unbinder = ButterKnife.bind(this, view);
        pbLoading.setAnimation(AnimationUtils.loadAnimation(pbLoading.getContext(), R.anim.rotate));
        initGlide();
        singleComicPresenter.loadXkcd(id);

        if (savedInstanceState != null) {
            dialogFragment = (SimpleInfoDialogFragment) getChildFragmentManager().findFragmentByTag("AltInfoDialogFragment");
            if (dialogFragment != null) {
                dialogFragment.setListener(dialogListener);
            }
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        singleComicPresenter.onDestroy();
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        if (searchView == null) {
            return;
        }
        searchView.setQueryHint(searchHint);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        if (searchAdapter == null) {
            searchAdapter = new SearchCursorAdapter(getActivity(), null, 0);
        }
        searchView.setSuggestionsAdapter(searchAdapter);
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                XkcdPic xkcd = searchSuggestions.get(position);
                // searchView.setQuery(xkcd, true);
                searchView.clearFocus();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(XKCD_INDEX_ON_NEW_INTENT, (int) xkcd.num);
                startActivity(intent);
                searchItem.collapseActionView();
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    return true;
                }
                singleComicPresenter.searchXkcd(newText);
                return true;
            }
        });
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                setItemsVisibility(menu, new int[]{R.id.action_left, R.id.action_right, R.id.action_share}, false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                setItemsVisibility(menu, new int[]{R.id.action_left, R.id.action_right, R.id.action_share}, true);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (currentPic == null) {
            return false;
        }
        switch (item.getItemId()) {
            case R.id.action_share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text, currentPic.getTargetImg()));
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_to)));
                logUXEvent(FIRE_SHARE_BAR);
                return true;
            case R.id.action_go_xkcd: {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://xkcd.com/" + currentPic.num));
                startActivity(browserIntent);
                logUXEvent(FIRE_GO_XKCD_MENU);
                return true;
            }
            case R.id.action_go_explain: {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.explainxkcd.com/wiki/index.php/" + currentPic.num));
                startActivity(browserIntent);
                logUXEvent(FIRE_GO_EXPLAIN_MENU);
                return true;
            }
        }
        return false;
    }

    public void renderXkcdSearch(List<XkcdPic> xkcdPics) {
        searchSuggestions = xkcdPics;
        String[] columns = {BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA,
        };
        MatrixCursor cursor = new MatrixCursor(columns);
        for (int i = 0; i < searchSuggestions.size(); i++) {
            XkcdPic xkcdPic = searchSuggestions.get(i);
            String[] tmp = {Integer.toString(i), xkcdPic.getTargetImg(), xkcdPic.getTitle(), String.valueOf(xkcdPic.num)};
            cursor.addRow(tmp);
        }
        searchAdapter.swapCursor(cursor);
    }

    @OnLongClick(R.id.iv_xkcd_pic)
    public boolean showExplainDialog(ImageView v) {
        if (currentPic == null) {
            return false;
        }
        dialogFragment = new SimpleInfoDialogFragment();
        dialogFragment.setPic(currentPic);
        dialogFragment.setListener(dialogListener);
        dialogFragment.show(getChildFragmentManager(), "AltInfoDialogFragment");
        v.performHapticFeedback(LONG_PRESS, FLAG_IGNORE_GLOBAL_SETTING);
        logUXEvent(FIRE_LONG_PRESS);
        return true;
    }

    private void setItemsVisibility(Menu menu, int[] hideItems, boolean visible) {
        for (int hideItem : hideItems) {
            menu.findItem(hideItem).setVisible(visible);
        }
    }

    private void initGlide() {
        target = new MyProgressTarget<>(new BitmapImageViewTarget(ivXkcdPic), pbLoading, ivXkcdPic);
    }

    /**
     * Launch a new Activity to show the pic in full screen mode
     */
    private void launchDetailPageActivity() {
        if (currentPic == null || TextUtils.isEmpty(currentPic.getTargetImg())) {
            return;
        }
        Intent intent = new Intent(getActivity(), ImageDetailPageActivity.class);
        intent.putExtra("URL", currentPic.getTargetImg());
        intent.putExtra("ID", currentPic.num);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    public void setLoading(boolean isLoading) {
        if (isLoading) {
            pbLoading.setVisibility(View.VISIBLE);
        } else {
            pbLoading.setVisibility(View.GONE);
        }
    }

    /**
     * Render img, text on the view
     *
     * @param xPic
     */
    public void renderXkcdPic(final XkcdPic xPic) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        if (TextUtils.isEmpty(target.getModel())) {
            target.setModel(xPic.getTargetImg());
            Glide.with(getActivity()).load(xPic.getTargetImg()).asBitmap().fitCenter().diskCacheStrategy(DiskCacheStrategy.SOURCE).listener(glideListener).into(target);
        }

        currentPic = xPic;
        Timber.i("Pic to be loaded: %d - %s", id, xPic.getTargetImg());
        tvTitle.setText(String.format("%d. %s", xPic.num, xPic.getTitle()));
        tvCreateDate.setText(String.format(getString(R.string.created_on), xPic.year, xPic.month, xPic.day));
        if (tvDescription != null) {
            tvDescription.setText(xPic.alt);
        }

    }

    private void logUXEvent(String event) {
        Bundle bundle = new Bundle();
        bundle.putString(FIRE_UX_ACTION, event);
        mFirebaseAnalytics.logEvent(FIRE_UX_ACTION, bundle);
    }

    private static class MyProgressTarget<Z> extends ProgressTarget<String, Z> {
        private final ProgressBar progressbar;
        private final ImageView image;

        public MyProgressTarget(Target<Z> target, ProgressBar progress, ImageView image) {
            super(target);
            this.progressbar = progress;
            this.image = image;
        }

        @Override
        public float getGranualityPercentage() {
            return 0.1f; // this matches the format string for #text below
        }

        @Override
        public void onDownloadStart() {

        }

        @Override
        public void onProgress(int progress) {

        }

        @Override
        public void onDownloadFinish() {

        }

        @Override
        protected void onConnecting() {
            progressbar.setProgress(1);
            progressbar.setVisibility(View.VISIBLE);
            image.setImageLevel(0);
        }

        @Override
        protected void onDownloading(long bytesRead, long expectedLength) {
            int progress = (int) (100 * bytesRead / expectedLength);
            progress = progress <= 0 ? 1 : progress;
            progressbar.setProgress(progress);
            if (progressbar.getAnimation() == null) {
                progressbar.setAnimation(AnimationUtils.loadAnimation(progressbar.getContext(), R.anim.rotate));
            }
            image.setImageLevel((int) (10000 * bytesRead / expectedLength));
        }

        @Override
        protected void onDownloaded() {
            image.setImageLevel(10000);
        }

        @Override
        protected void onDelivered() {
            progressbar.setVisibility(View.INVISIBLE);
            progressbar.clearAnimation();
            image.setImageLevel(0); // reset ImageView default
        }
    }
}
