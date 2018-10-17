package xyz.jienan.xkcd.comics.presenter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.text.TextUtils;

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

    private static final int STEP = 150;

    private final XkcdModel xkcdModel = XkcdModel.getInstance();

    private ImageDetailPageContract.View view;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Movie mMovie;

    private int movieWidth;

    private int movieHeight;

    private int currentFrame = 1;

    private int stepMultiplier = 1;

    private int duration;

    public ImageDetailPagePresenter(ImageDetailPageContract.View imageDetailPageActivity) {
        view = imageDetailPageActivity;
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
                    duration = Math.min(mMovie.duration(), Integer.MAX_VALUE);
                    view.renderSeekBar(duration);
                }, Timber::e));
    }

    @Override
    public void parseFrame(int progress) {
        currentFrame = progress;
        if (movieHeight == 0 || movieWidth == 0) {
            movieHeight = mMovie.height();
            movieWidth = mMovie.width();
        }

        final Bitmap bitmap = Bitmap.createBitmap(movieWidth, movieHeight, Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(bitmap);
        canvas.setBitmap(bitmap);
        mMovie.setTime(progress);
        mMovie.draw(canvas, 0, 0);

        view.renderFrame(bitmap);
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
        int progress = isForward ? currentFrame + STEP * stepMultiplier : currentFrame - STEP * stepMultiplier;
        progress = Math.min(Math.max(progress, 1), duration);
        view.changeGifSeekBarProgress(progress);
        parseFrame(progress);
        if (progress == 1 || progress == duration) {
            stepMultiplier = 1;
        }
    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();
    }
}
