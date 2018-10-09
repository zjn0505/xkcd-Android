package xyz.jienan.xkcd.comics.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
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

import butterknife.BindView;
import butterknife.OnLongClick;
import timber.log.Timber;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.base.BaseFragment;
import xyz.jienan.xkcd.base.glide.ProgressTarget;
import xyz.jienan.xkcd.comics.activity.ImageDetailPageActivity;
import xyz.jienan.xkcd.comics.contract.SingleComicContract;
import xyz.jienan.xkcd.comics.dialog.SimpleInfoDialogFragment;
import xyz.jienan.xkcd.comics.dialog.SimpleInfoDialogFragment.ISimpleInfoDialogListener;
import xyz.jienan.xkcd.comics.presenter.SingleComicPresenter;
import xyz.jienan.xkcd.model.XkcdPic;

import static android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
import static android.view.HapticFeedbackConstants.LONG_PRESS;
import static xyz.jienan.xkcd.Const.FIRE_GO_EXPLAIN_MENU;
import static xyz.jienan.xkcd.Const.FIRE_GO_XKCD_MENU;
import static xyz.jienan.xkcd.Const.FIRE_LONG_PRESS;
import static xyz.jienan.xkcd.Const.FIRE_MORE_EXPLAIN;
import static xyz.jienan.xkcd.Const.FIRE_SHARE_BAR;
import static xyz.jienan.xkcd.base.network.NetworkService.XKCD_BASE_URL;
import static xyz.jienan.xkcd.base.network.NetworkService.XKCD_EXPLAIN_URL;

/**
 * Created by jienanzhang on 03/03/2018.
 */

public class SingleComicFragment extends BaseFragment implements SingleComicContract.View {

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

    private SimpleInfoDialogFragment dialogFragment;

    private int id;

    private XkcdPic currentPic;

    private ProgressTarget<String, Bitmap> target;

    private SimpleInfoDialogFragment.ExplainingCallback explainingCallback;

    private SingleComicContract.Presenter singleComicPresenter;

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
        public boolean onResourceReady(Bitmap resource, String model,
                                       Target<Bitmap> target, boolean isFromMemoryCache,
                                       boolean isFirstResource) {
            if (ivXkcdPic != null) {
                ivXkcdPic.setOnClickListener(v -> launchDetailPageActivity());
            }
            singleComicPresenter.updateXkcdSize(currentPic, resource);
            return false;
        }
    };
    private ISimpleInfoDialogListener dialogListener = new ISimpleInfoDialogListener() {
        @Override
        public void onPositiveClick() {
            // Do nothing
        }

        @Override
        public void onNegativeClick() {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(XKCD_EXPLAIN_URL + currentPic.num));
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

    public static SingleComicFragment newInstance(int comicId) {
        SingleComicFragment fragment = new SingleComicFragment();
        Bundle args = new Bundle();
        args.putInt("id", comicId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void explainLoaded(String result) {
        if (!TextUtils.isEmpty(result)) {
            explainingCallback.explanationLoaded(result);
        } else {
            explainingCallback.explanationFailed();
        }
    }

    @Override
    public void explainFailed() {
        explainingCallback.explanationFailed();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        singleComicPresenter = new SingleComicPresenter(this);
        Bundle args = getArguments();
        if (args != null) {
            id = args.getInt("id");
        }
        setHasOptionsMenu(true);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_comic_single;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        pbLoading.clearAnimation();
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
        singleComicPresenter.onDestroy();
        Glide.clear(target);
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (dialogFragment != null && dialogFragment.isAdded()) {
            dialogFragment.dismiss();
            dialogFragment = null;
        }
        super.onSaveInstanceState(outState);
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
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(XKCD_BASE_URL + currentPic.num));
                startActivity(browserIntent);
                logUXEvent(FIRE_GO_XKCD_MENU);
                return true;
            }
            case R.id.action_go_explain: {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(XKCD_EXPLAIN_URL + currentPic.num));
                startActivity(browserIntent);
                logUXEvent(FIRE_GO_EXPLAIN_MENU);
                return true;
            }
            default:
                break;
        }
        return false;
    }

    @Override
    public void setLoading(boolean isLoading) {
        if (isLoading) {
            pbLoading.setVisibility(View.VISIBLE);
        } else {
            pbLoading.setVisibility(View.GONE);
        }
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
        ImageDetailPageActivity.startActivity(getActivity(), currentPic.getTargetImg(), currentPic.num);
        getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    /**
     * Render img, text on the view
     *
     * @param xPic
     */
    @Override
    public void renderXkcdPic(final XkcdPic xPic) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        if (TextUtils.isEmpty(target.getModel())) {
            target.setModel(xPic.getTargetImg());
            Glide.with(getActivity())
                    .load(xPic.getTargetImg())
                    .asBitmap()
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .listener(glideListener)
                    .into(target);
        }

        currentPic = xPic;
        Timber.i("Pic to be loaded: %d - %s", id, xPic.getTargetImg());
        tvTitle.setText(String.format("%d. %s", xPic.num, xPic.getTitle()));
        tvCreateDate.setText(String.format(getString(R.string.created_on), xPic.year, xPic.month, xPic.day));
        if (tvDescription != null) {
            tvDescription.setText(xPic.alt);
        }

    }

    private static class MyProgressTarget<Z> extends ProgressTarget<String, Z> {
        private final ProgressBar progressbar;
        private final ImageView image;

        MyProgressTarget(Target<Z> target, ProgressBar progress, ImageView image) {
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
