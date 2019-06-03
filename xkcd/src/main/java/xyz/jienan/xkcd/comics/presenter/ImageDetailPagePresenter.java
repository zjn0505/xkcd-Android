package xyz.jienan.xkcd.comics.presenter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.text.TextUtils;
import android.util.LruCache;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import xyz.jienan.xkcd.comics.contract.ImageDetailPageContract;
import xyz.jienan.xkcd.model.XkcdModel;
import xyz.jienan.xkcd.model.XkcdPic;

public class ImageDetailPagePresenter implements ImageDetailPageContract.Presenter {

    private final XkcdModel xkcdModel = XkcdModel.INSTANCE;

    private ImageDetailPageContract.View view;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Movie mMovie;

    private int movieWidth;

    private int movieHeight;

    private int currentFrame = 1;

    private int stepMultiplier = 1;

    private int duration;

    private Bitmap reusableBitmap = null;

    private Canvas canvas = null;

    private LruCache<Integer, Bitmap> mMemoryCache;

    private boolean isEcoMode = true;

    private int step = 150;

    public ImageDetailPagePresenter(ImageDetailPageContract.View imageDetailPageActivity) {
        view = imageDetailPageActivity;
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        mMemoryCache = new LruCache<Integer, Bitmap>(maxMemory / 8) {
            @Override
            protected int sizeOf(Integer key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    @Override
    public void requestImage(int index) {
        XkcdPic xkcdPicInDb = xkcdModel.loadXkcdFromDB(index);
        if (xkcdPicInDb == null
                || TextUtils.isEmpty(xkcdPicInDb.getTargetImg())
                || TextUtils.isEmpty(xkcdPicInDb.getTitle())) {
            Disposable d = xkcdModel.loadXkcd(index)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(ignored -> view.setLoading(true))
                    .subscribe(xkcdPic -> {
                                view.renderPic(xkcdPic.getTargetImg());
                                view.renderTitle(xkcdPic);
                            },
                            e -> Timber.e(e, "Request pic in detail page error, %d", index));
            compositeDisposable.add(d);
        } else {
            view.renderPic(xkcdPicInDb.getTargetImg());
            view.renderTitle(xkcdPicInDb);
        }
    }

    @Override
    public void parseGifData(byte[] bytes) {
        if (bytes == null || bytes.length <= 0) {
            return;
        }
        mMovie = Movie.decodeByteArray(bytes, 0, bytes.length);
        compositeDisposable.add(Observable.interval(100, TimeUnit.MILLISECONDS)
                .filter(ignored -> mMovie != null)
                .take(1)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ignored -> {
                    duration = Math.min(mMovie.duration(), Integer.MAX_VALUE) / step * getEcoModeValue();
                    view.renderSeekBar(duration);
                }, Timber::e));
    }

    @Override
    public void parseFrame(int progress) {
        progress = progress == 0 ? 1 : progress;
        currentFrame = progress;
        if (movieHeight == 0 || movieWidth == 0) {
            movieHeight = mMovie.height();
            movieWidth = mMovie.width();
        }

        Bitmap bitmap = getBitmapFromMemCache(progress);

        if (bitmap != null) {
            view.renderFrame(bitmap);
        } else {
            if (reusableBitmap == null || canvas == null) {
                reusableBitmap = Bitmap.createBitmap(movieWidth, movieHeight, Bitmap.Config.RGB_565);
                canvas = new Canvas(reusableBitmap);
            }

            mMovie.setTime(progress * step / getEcoModeValue());
            mMovie.draw(canvas, 0, 0);
            addBitmapToMemoryCache(progress, reusableBitmap);
            view.renderFrame(reusableBitmap);
        }
    }

    @Override
    public void adjustGifSpeed(int increaseByOne) {
        stepMultiplier = stepMultiplier + increaseByOne;
        if (increaseByOne == 0) {
            stepMultiplier = 1;
        }
        if (stepMultiplier == 0) {
            stepMultiplier = increaseByOne == 1 ? 1 : -1;
        }
        stepMultiplier = Math.max(Math.min(stepMultiplier, 8), -8);
        view.showGifPlaySpeed(stepMultiplier);
    }

    @Override
    public void adjustGifFrame(boolean isForward) {
        int progress = isForward ? currentFrame + getEcoModeValue() * stepMultiplier
                : currentFrame - getEcoModeValue() * stepMultiplier;
        progress = Math.min(Math.max(progress, 1), duration);
        progress = progress == 1 && isForward ? 2 : progress;
        view.changeGifSeekBarProgress(progress);
        parseFrame(progress);
        if (progress == 1 || progress == duration) {
            stepMultiplier = 1;
        }
    }

    @Override
    public void setEcoMode(boolean isEcoMode) {
        this.isEcoMode = isEcoMode;
        this.step = isEcoMode ? 150 : 90;
    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();
    }

    private void addBitmapToMemoryCache(int key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(int key) {
        return mMemoryCache.get(key);
    }

    private int getEcoModeValue() {
        return isEcoMode ?  1 : step;
    }
}
