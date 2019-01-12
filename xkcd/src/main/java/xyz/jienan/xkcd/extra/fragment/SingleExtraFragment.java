package xyz.jienan.xkcd.extra.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.OnLongClick;
import timber.log.Timber;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.base.BaseFragment;
import xyz.jienan.xkcd.base.glide.MyProgressTarget;
import xyz.jienan.xkcd.base.glide.ProgressTarget;
import xyz.jienan.xkcd.comics.activity.ImageDetailPageActivity;
import xyz.jienan.xkcd.comics.dialog.SimpleInfoDialogFragment;
import xyz.jienan.xkcd.comics.dialog.SimpleInfoDialogFragment.ISimpleInfoDialogListener;
import xyz.jienan.xkcd.extra.contract.SingleExtraContract;
import xyz.jienan.xkcd.extra.presenter.SingleExtraPresenter;
import xyz.jienan.xkcd.model.ExtraComics;
import xyz.jienan.xkcd.model.util.ExplainLinkUtil;

import static android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
import static android.view.HapticFeedbackConstants.LONG_PRESS;
import static xyz.jienan.xkcd.Const.FIRE_EXTRA_SUFFIX;
import static xyz.jienan.xkcd.Const.FIRE_GO_EXTRA_MENU;
import static xyz.jienan.xkcd.Const.FIRE_LONG_PRESS;
import static xyz.jienan.xkcd.Const.FIRE_SHARE_BAR;

/**
 * Created by jienanzhang on 03/03/2018.
 */

public class SingleExtraFragment extends BaseFragment implements SingleExtraContract.View {

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

    private ExtraComics currentExtra;

    private ProgressTarget<String, Bitmap> target;

    private SingleExtraContract.Presenter singleExtraPresenter;

    private static class GlideListener implements RequestListener<String, Bitmap> {

        private WeakReference<SingleExtraFragment> weakReference;

        GlideListener(SingleExtraFragment singleExtraFragment) {
            weakReference = new WeakReference<>(singleExtraFragment);
        }

        @Override
        public boolean onException(Exception e, final String model,
                                   final Target<Bitmap> target, boolean isFirstResource) {

            SingleExtraFragment fragment = weakReference.get();

            if (fragment == null) {
                return false;
            }

            if (fragment.btnReload == null) {
                return false;
            }

            if (model.startsWith("https")) {
                fragment.load(model.replaceFirst("https", "http"));
                return true;
            }

            fragment.btnReload.setVisibility(View.VISIBLE);

            fragment.btnReload.setOnClickListener(view -> {
                fragment.pbLoading.clearAnimation();
                fragment.pbLoading.setAnimation(AnimationUtils.loadAnimation(fragment.pbLoading.getContext(), R.anim.rotate));
                Glide.with(fragment.getActivity()).load(fragment.currentExtra.img).asBitmap().fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE).listener(this).into(target);

            });
            return false;
        }

        @Override
        public boolean onResourceReady(Bitmap resource, String model,
                                       Target<Bitmap> target, boolean isFromMemoryCache,
                                       boolean isFirstResource) {
            SingleExtraFragment fragment = weakReference.get();

            if (fragment == null) {
                return false;
            }
            fragment.btnReload.setVisibility(View.GONE);
            if (fragment.ivXkcdPic != null) {
                fragment.ivXkcdPic.setOnClickListener(v -> fragment.launchDetailPageActivity());
            }
            return false;
        }
    }

    private ISimpleInfoDialogListener dialogListener = new ISimpleInfoDialogListener() {
        @Override
        public void onPositiveClick() {
            // Do nothing
        }

        @Override
        public void onNegativeClick() {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentExtra.explainUrl));
            if (browserIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(browserIntent);
            }
        }

        @Override
        public void onExplainMoreClick(final SimpleInfoDialogFragment.ExplainingCallback explainingCallback) {
            // no-ops
        }
    };

    public static SingleExtraFragment newInstance(int comicId) {
        SingleExtraFragment fragment = new SingleExtraFragment();
        Bundle args = new Bundle();
        args.putInt("id", comicId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void explainLoaded(String result) {
        if (!TextUtils.isEmpty(result)) {
            if (dialogFragment != null && dialogFragment.isAdded()) {
                dialogFragment.setExtraExplain(result);
            }
            if (tvDescription != null) {
                ExplainLinkUtil.setTextViewHTML(tvDescription, result);
            }
        } else {
            if (dialogFragment != null && dialogFragment.isAdded()) {
                dialogFragment.setExtraExplain(null);
            }
        }
    }

    @Override
    public void explainFailed() {
        // no-ops
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        singleExtraPresenter = new SingleExtraPresenter(this);
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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        pbLoading.clearAnimation();
        pbLoading.setAnimation(AnimationUtils.loadAnimation(pbLoading.getContext(), R.anim.rotate));
        initGlide();
        singleExtraPresenter.loadExtra(id);

        if (savedInstanceState != null) {
            dialogFragment = (SimpleInfoDialogFragment) getChildFragmentManager()
                    .findFragmentByTag("AltInfoDialogFragment");
            if (dialogFragment != null) {
                dialogFragment.setListener(dialogListener);
            }
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        singleExtraPresenter.onDestroy();
        Glide.clear(target);
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (dialogFragment != null && dialogFragment.isAdded()) {
            dialogFragment.dismissAllowingStateLoss();
            dialogFragment = null;
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (currentExtra == null) {
            return false;
        }
        switch (item.getItemId()) {
            case R.id.action_share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text, currentExtra.img));
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_to)));
                logUXEvent(FIRE_SHARE_BAR + FIRE_EXTRA_SUFFIX);
                return true;
            case R.id.action_go_explain: {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentExtra.explainUrl));
                startActivity(browserIntent);
                logUXEvent(FIRE_GO_EXTRA_MENU);
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
        if (currentExtra == null) {
            return false;
        }
        dialogFragment = new SimpleInfoDialogFragment();
        dialogFragment.setListener(dialogListener);
        dialogFragment.show(getChildFragmentManager(), "AltInfoDialogFragment");
        v.performHapticFeedback(LONG_PRESS, FLAG_IGNORE_GLOBAL_SETTING);
        singleExtraPresenter.getExplain(currentExtra.explainUrl);
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
        if (currentExtra == null || TextUtils.isEmpty(currentExtra.img)) {
            return;
        }
        ImageDetailPageActivity.startActivity(getActivity(), currentExtra.img, currentExtra.num, true);
        getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    /**
     * Render img, text on the view
     *
     * @param xPic
     */
    @Override
    public void renderExtraPic(final ExtraComics xPic) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        if (TextUtils.isEmpty(target.getModel())) {
            target.setModel(xPic.img);
            load(xPic.img);
        }

        currentExtra = xPic;
        Timber.i("Pic to be loaded: %d - %s", id, xPic.img);
        tvTitle.setText(String.format("%d. %s", xPic.num, xPic.title));
        tvCreateDate.setText(xPic.date);
        singleExtraPresenter.getExplain(xPic.explainUrl);
    }

    private void load(@NonNull String url) {
        Glide.with(getActivity())
                .load(url)
                .asBitmap()
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(new GlideListener(this))
                .into(target);
    }
}
