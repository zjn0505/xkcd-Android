package xyz.jienan.xkcd.comics.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.piasy.biv.view.BigImageView;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import timber.log.Timber;
import xyz.jienan.xkcd.BuildConfig;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.base.BaseActivity;
import xyz.jienan.xkcd.base.glide.GlideUtils;
import xyz.jienan.xkcd.comics.contract.ImageDetailPageContract;
import xyz.jienan.xkcd.comics.presenter.ImageDetailPagePresenter;
import xyz.jienan.xkcd.model.XkcdPic;
import xyz.jienan.xkcd.model.util.XkcdSideloadUtils;
import xyz.jienan.xkcd.ui.AnimUtils;
import xyz.jienan.xkcd.ui.ToastUtils;

import static xyz.jienan.xkcd.Const.FIRE_COMIC_ID;
import static xyz.jienan.xkcd.Const.FIRE_COMIC_URL;
import static xyz.jienan.xkcd.Const.FIRE_DETAIL_PAGE;
import static xyz.jienan.xkcd.Const.FIRE_GIF_FAST_FORWARD;
import static xyz.jienan.xkcd.Const.FIRE_GIF_FAST_REWIND;
import static xyz.jienan.xkcd.Const.FIRE_GIF_NEXT_CLICK;
import static xyz.jienan.xkcd.Const.FIRE_GIF_NEXT_HOLD;
import static xyz.jienan.xkcd.Const.FIRE_GIF_PREVIOUS_CLICK;
import static xyz.jienan.xkcd.Const.FIRE_GIF_PREVIOUS_HOLD;
import static xyz.jienan.xkcd.Const.FIRE_GIF_USER_PROGRESS;
import static xyz.jienan.xkcd.Const.FIRE_LARGE_IMAGE;
import static xyz.jienan.xkcd.Const.PREF_XKCD_GIF_ECO;

/**
 * Created by jienanzhang on 09/07/2017.
 */

public class ImageDetailPageActivity extends BaseActivity implements ImageDetailPageContract.View {

    private static final String KEY_URL = "URL";

    private static final String KEY_ID = "ID";

    private static final String KEY_LARGE = "LARGE";

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

    @BindView(R.id.sb_movie)
    SeekBar sbMovie;

    @BindView(R.id.rl_gif_panel)
    LinearLayout gifPanel;

    @BindView(R.id.btn_gif_play)
    ImageView playBtn;

    private View.OnClickListener listener = v -> {
        finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    };

    private int index;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Disposable holdDisposable = Disposables.disposed();

    private ImageDetailPageContract.Presenter imageDetailPagePresenter;

    private boolean showTitle = false;

    private RequestManager glide;

    private String url = "";

    private boolean isEcoMode = true;

    private boolean withLargeImage = false;

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

    public static void startActivity(Context context,
                                     @Nullable String url,
                                     @NonNull Long id,
                                     boolean withLargeImage) {
        if (id <= 0) {
            return;
        }
        Intent intent = new Intent(context, ImageDetailPageActivity.class);
        intent.putExtra(KEY_URL, url);
        intent.putExtra(KEY_ID, id);
        intent.putExtra(KEY_LARGE, withLargeImage);
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
        isEcoMode = sharedPreferences.getBoolean(PREF_XKCD_GIF_ECO, true);
        imageDetailPagePresenter.setEcoMode(isEcoMode);
        setContentView(R.layout.activity_image_detail);
        ButterKnife.bind(this);
        glide = Glide.with(this);
        url = getIntent().getStringExtra(KEY_URL);
        index = (int) getIntent().getLongExtra(KEY_ID, 0L);
        showTitle = getIntent().getBooleanExtra(KEY_SHOW_TITLE, false);
        withLargeImage = getIntent().getBooleanExtra(KEY_LARGE, false);
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
            if (isGifInPlayState()) {
                setGifPlayState(false);
            }
            stopPlayingGif();
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
        if (BuildConfig.DEBUG && !url.endsWith("gif")) {
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
        gifPanel.setVisibility(View.GONE);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    @Override
    protected void onDestroy() {
        if (photoView.getVisibility() == View.VISIBLE) {
            Glide.clear(photoView);
        }
        imageDetailPagePresenter.onDestroy();
        setGifPlayState(false);
        compositeDisposable.dispose();
        super.onDestroy();
    }

    @Override
    public void renderPic(String url) {
        this.url = url;
        Bundle bundle = new Bundle();
        if (XkcdSideloadUtils.useLargeImageView(index) && !withLargeImage) {
            bigImageView.showImage(Uri.parse(url));
            bigImageView.setVisibility(View.VISIBLE);
            photoView.setVisibility(View.GONE);
            photoView.setEnabled(false);
            bundle.putBoolean(FIRE_LARGE_IMAGE, true);
        } else {
            bigImageView.setVisibility(View.GONE);
            bigImageView.setEnabled(false);
            photoView.setVisibility(View.VISIBLE);
            bundle.putBoolean(FIRE_LARGE_IMAGE, false);

            if (url.endsWith("gif")) {
                loadGifWithControl();
            } else {
                loadGifWithoutControl(url);
            }
        }

        bundle.putInt(FIRE_COMIC_ID, index);
        bundle.putString(FIRE_COMIC_URL, url);
        logUXEvent(FIRE_DETAIL_PAGE, bundle);

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
    public void renderSeekBar(int duration) {
        pbLoading.setVisibility(View.GONE);
        gifPanel.setVisibility(View.VISIBLE);
        sbMovie.setVisibility(View.VISIBLE);
        sbMovie.setMax(duration);
        sbMovie.setOnSeekBarChangeListener(new GifSeekBarListener());
        imageDetailPagePresenter.parseFrame(1);
        onGifPlayClicked();
    }

    @Override
    protected void onPause() {
        if (isGifInPlayState()) {
            onGifPlayClicked();
        }
        super.onPause();
    }

    @Override
    public void renderFrame(Bitmap bitmap) {
        photoView.setImageBitmap(bitmap);
        sbMovie.setThumb(new BitmapDrawable(getResources(),
                Bitmap.createScaledBitmap(bitmap, 100, 100, false)));
    }

    @Override
    public void changeGifSeekBarProgress(int progress) {
        sbMovie.setProgress(progress);
    }

    @Override
    public void showGifPlaySpeed(int speed) {
        if (isGifInPlayState()) {
            ToastUtils.showToast(this, String.format(speed < 0 ? "<< %d" : "%d >>", speed));
        } else {
            ToastUtils.cancelToast();
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

    @OnClick({R.id.btn_gif_back, R.id.btn_gif_forward})
    public void onGifSpeedClicked(ImageView view) {
        boolean isForward = view.getId() == R.id.btn_gif_forward;
        if (isGifInPlayState()) {
            imageDetailPagePresenter.adjustGifSpeed(isForward ? 1 : -1);
            logUXEvent(isForward ? FIRE_GIF_FAST_FORWARD : FIRE_GIF_FAST_REWIND);
        } else {
            imageDetailPagePresenter.adjustGifSpeed(0);
            imageDetailPagePresenter.adjustGifFrame(isForward);
            logUXEvent(isForward ? FIRE_GIF_NEXT_CLICK : FIRE_GIF_PREVIOUS_CLICK);
        }
        AnimUtils.vectorAnim(view, isForward ? R.drawable.anim_fast_forward_shake : R.drawable.anim_fast_rewind_shake);
    }

    @OnTouch({R.id.btn_gif_back, R.id.btn_gif_forward})
    public boolean onGifSpeedPressed(View view, MotionEvent motionEvent) {
        if (!isGifInPlayState()) {
            if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL || motionEvent.getAction() == MotionEvent.ACTION_UP) {
                stopPlayingGif();
            } else if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                startPlayingGif(view.getId() == R.id.btn_gif_forward, true);
            }
        }
        return false;
    }

    @OnClick(R.id.btn_gif_play)
    public void onGifPlayClicked() {
        final boolean isNewStatePlay = !isGifInPlayState();
        setGifPlayState(isNewStatePlay);
        stopPlayingGif();
        if (isNewStatePlay) {
            startPlayingGif(true, false);
        }
    }

    private void startPlayingGif(boolean isForward, boolean isFromUserLongPress) {
        stopPlayingGif();
        holdDisposable = Observable.interval(isEcoMode ? 100 : 60, TimeUnit.MILLISECONDS)
                .delay(isFromUserLongPress ? 500 : 0, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(ignored -> {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    if (!isFromUserLongPress) {
                        AnimUtils.vectorAnim(playBtn, R.drawable.anim_play_to_pause, R.drawable.ic_pause);
                    }
                })
                .doOnDispose(() -> {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    imageDetailPagePresenter.adjustGifSpeed(0);
                    if (!isFromUserLongPress) {
                        AnimUtils.vectorAnim(playBtn, R.drawable.anim_pause_to_play, R.drawable.ic_play_arrow);
                    }
                })
                .doOnNext(num -> {
                    if (num == 10 && isFromUserLongPress) {
                        logUXEvent(isForward ? FIRE_GIF_NEXT_HOLD : FIRE_GIF_PREVIOUS_HOLD);
                    }
                })
                .subscribe(ignored -> {
                    if (!isFromUserLongPress && sbMovie.getProgress() == sbMovie.getMax() && isForward) {
                        imageDetailPagePresenter.parseFrame(1);
                    }
                    imageDetailPagePresenter.adjustGifFrame(isForward);
                }, e -> Timber.e("Failed to play gif"));
        compositeDisposable.add(holdDisposable);
    }

    private void stopPlayingGif() {
        if (!holdDisposable.isDisposed()) {
            holdDisposable.dispose();
        }
    }

    private boolean isGifInPlayState() {
        return !playBtn.getTag().toString().equals("0");
    }

    private void setGifPlayState(boolean isPlay) {
        playBtn.setTag(isPlay ? "1" : "0");
    }

    private boolean equalWithinError(float scale, float target) {
        final float errorMargin = 0.16f;
        return scale - target < errorMargin;
    }

    private void loadGifWithControl() {
        pbLoading.setVisibility(View.VISIBLE);
        GlideUtils.loadGif(glide, url, new SimpleTarget<GifDrawable>() {

            @Override
            public void onResourceReady(GifDrawable resource, GlideAnimation<? super GifDrawable> glideAnimation) {
                imageDetailPagePresenter.parseGifData(resource.getData());
            }
        });
    }

    private void loadGifWithoutControl(String url) {
        pbLoading.setVisibility(View.VISIBLE);
        glide.load(url)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e,
                                               String model,
                                               Target<GlideDrawable> target,
                                               boolean isFirstResource) {
                        if (model.startsWith("https")) {
                            loadGifWithoutControl(model.replaceFirst("https", "http"));
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
    }

    private class GifSeekBarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                imageDetailPagePresenter.parseFrame(progress);
                if (isGifInPlayState()) {
                    setGifPlayState(false);
                }
                stopPlayingGif();
            } else {
                if (seekBar.getProgress() == 1 || seekBar.getProgress() == seekBar.getMax()) {
                    if (isGifInPlayState()) {
                        setGifPlayState(false);
                    }
                    stopPlayingGif();
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // no ops
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            logUXEvent(FIRE_GIF_USER_PROGRESS);
        }
    }
}
