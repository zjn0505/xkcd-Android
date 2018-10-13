package xyz.jienan.xkcd.comics.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.piasy.biv.view.BigImageView;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;
import xyz.jienan.xkcd.BuildConfig;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.comics.contract.ImageDetailPageContract;
import xyz.jienan.xkcd.comics.presenter.ImageDetailPagePresenter;
import xyz.jienan.xkcd.model.XkcdPic;
import xyz.jienan.xkcd.model.util.XkcdSideloadUtils;

import static xyz.jienan.xkcd.Const.FIRE_COMIC_ID;
import static xyz.jienan.xkcd.Const.FIRE_COMIC_URL;
import static xyz.jienan.xkcd.Const.FIRE_DETAIL_PAGE;
import static xyz.jienan.xkcd.Const.FIRE_LARGE_IMAGE;

/**
 * Created by jienanzhang on 09/07/2017.
 */

public class ImageDetailPageActivity extends Activity implements ImageDetailPageContract.View {

    private static final String KEY_URL = "URL";

    private static final String KEY_ID = "ID";

    private static final String KEY_SHOW_TITLE = "show_title";

    private static final int MAX_SCALE = 10;

    private static final int TRANSITION_ANIMATION_DURATION = 500;

    @BindView(R.id.photo_view)
    PhotoView photoView;

    @BindView(R.id.big_image_view)
    BigImageView bigImageView;

    @BindView(R.id.pb_loading)
    ProgressBar pbLoading;

    @BindView(R.id.tv_title)
    TextView tvTitle;

    private int index;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private FirebaseAnalytics mFirebaseAnalytics;

    private ImageDetailPageContract.Presenter imageDetailPagePresenter;

    private boolean showTitle = false;

    public static void startActivity(Context context,
                                     @Nullable String url,
                                     @NonNull Long id) {
        if (id <= 0) {
            return;
        }
        Intent intent = new Intent(context, ImageDetailPageActivity.class);
        intent.putExtra(KEY_URL, url);
        intent.putExtra(KEY_ID, id);
        context.startActivity(intent);
    }

    public static void startActivityFromId(Context context, @NonNull Long id) {
        if (id <= 0) {
            return;
        }
        Intent intent = new Intent(context, ImageDetailPageActivity.class);
        intent.putExtra(KEY_ID, id);
        intent.putExtra(KEY_SHOW_TITLE, true);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageDetailPagePresenter = new ImageDetailPagePresenter(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setContentView(R.layout.activity_image_detail);
        ButterKnife.bind(this);
        final String url = getIntent().getStringExtra(KEY_URL);
        index = (int) getIntent().getLongExtra(KEY_ID, 0L);
        showTitle = getIntent().getBooleanExtra(KEY_SHOW_TITLE, false);
        photoView.setMaximumScale(MAX_SCALE);
        photoView.setZoomTransitionDuration(TRANSITION_ANIMATION_DURATION);
        if (!TextUtils.isEmpty(url)) {
            renderPic(url);
        } else if (index != 0) {
            imageDetailPagePresenter.requestImage(index);
        } else {
            Timber.e("No valid info for detail page");
            finish();
        }
        photoView.setOnScaleChangeListener((scaleFactor, focusX, focusY) -> {
            if (photoView.isEnabled() && showTitle) {
                tvTitle.setVisibility(equalWithinError(photoView.getScale(), 1) ? View.VISIBLE : View.GONE);
            }
        });
        if (bigImageView.getSSIV() != null) {
            bigImageView.getSSIV().setMaxScale(MAX_SCALE);
            bigImageView.getSSIV().setOnStateChangedListener(new SubsamplingScaleImageView.DefaultOnStateChangedListener() {

                private float initScale = 0.0f;

                @Override
                public void onScaleChanged(float newScale, int origin) {
                    super.onScaleChanged(newScale, origin);
                    if (bigImageView.isEnabled() && showTitle) {
                        if (initScale == 0) {
                            initScale = newScale;
                        }
                        tvTitle.setVisibility(equalWithinError(newScale, initScale) ? View.VISIBLE : View.GONE);
                    }
                }
            });
        }
        if (BuildConfig.DEBUG) {
            photoView.setOnLongClickListener(ignored -> {
                bigImageView.showImage(Uri.parse(url));
                bigImageView.setVisibility(View.VISIBLE);
                photoView.setVisibility(View.GONE);
                photoView.setEnabled(false);
                return true;
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    @Override
    protected void onDestroy() {
        if (photoView.getVisibility() == View.VISIBLE) {
            Glide.clear(photoView);
        }
        imageDetailPagePresenter.onDestroy();
        compositeDisposable.dispose();
        super.onDestroy();
    }

    @Override
    public void renderPic(String url) {
        Bundle bundle = new Bundle();
        if (XkcdSideloadUtils.useLargeImageView(index)) {
            bigImageView.showImage(Uri.parse(url));
            bigImageView.setVisibility(View.VISIBLE);
            photoView.setVisibility(View.GONE);
            photoView.setEnabled(false);
            bundle.putBoolean(FIRE_LARGE_IMAGE, true);
        } else {
            Glide.with(this)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e,
                                                   String model,
                                                   Target<GlideDrawable> target,
                                                   boolean isFirstResource) {
                            if (model.startsWith("https")) {
                                Glide.with(ImageDetailPageActivity.this)
                                        .load(model.replaceFirst("https", "http"))
                                        .listener(this)
                                        .into(photoView);
                                return true;
                            }
                            pbLoading.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource,
                                                       String model,
                                                       Target<GlideDrawable> target,
                                                       boolean isFromMemoryCache,
                                                       boolean isFirstResource) {
                            pbLoading.setVisibility(View.GONE);
                            return false;
                        }
                    }).into(photoView);
            bigImageView.setVisibility(View.GONE);
            bigImageView.setEnabled(false);
            photoView.setVisibility(View.VISIBLE);
            bundle.putBoolean(FIRE_LARGE_IMAGE, false);
        }
        bundle.putInt(FIRE_COMIC_ID, index);
        bundle.putString(FIRE_COMIC_URL, url);
        mFirebaseAnalytics.logEvent(FIRE_DETAIL_PAGE, bundle);
        final View.OnClickListener listener = v -> {
            ImageDetailPageActivity.this.finish();
            ImageDetailPageActivity.this.overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        };
        compositeDisposable.add(Observable.timer(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread()).subscribe(ignored -> {
                    photoView.setOnClickListener(listener);
                    bigImageView.setOnClickListener(listener);
                }, e -> Timber.e(e, "add listener error")));
    }

    @Override
    public void renderTitle(XkcdPic xkcdPic) {
        if (showTitle) {
            tvTitle.setText(getString(R.string.item_search_title, String.valueOf(xkcdPic.num), xkcdPic.getTitle()));
            tvTitle.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setLoading(boolean isLoading) {
        if (isLoading) {
            pbLoading.setVisibility(View.VISIBLE);
        } else {
            pbLoading.setVisibility(View.GONE);
        }
    }

    private boolean equalWithinError(float scale, float target) {
        final float errorMargin = 0.16f;
        return scale - target < errorMargin;
    }
}
