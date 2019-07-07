package xyz.jienan.xkcd.comics.presenter

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Movie
import android.util.LruCache
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xyz.jienan.xkcd.comics.contract.ImageDetailPageContract
import xyz.jienan.xkcd.model.XkcdModel
import java.util.concurrent.TimeUnit
import kotlin.math.min

class ImageDetailPagePresenter(private val view: ImageDetailPageContract.View) : ImageDetailPageContract.Presenter {

    private val compositeDisposable = CompositeDisposable()

    private var mMovie: Movie? = null

    private val movieWidth by lazy { mMovie!!.width() }

    private val movieHeight by lazy { mMovie!!.height() }

    private var currentFrame = 1

    private var stepMultiplier = 1

    private var duration = 0

    private var reusableBitmap: Bitmap? = null

    private var canvas: Canvas? = null

    private val mMemoryCache: LruCache<Int, Bitmap>

    override var isEcoMode = true

    private val step
        get() = if (isEcoMode) 150 else 90

    private val ecoModeValue
        get() = if (isEcoMode) 1 else step

    init {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        mMemoryCache = object : LruCache<Int, Bitmap>(maxMemory / 8) {
            // The cache size will be measured in kilobytes rather than number of items.
            override fun sizeOf(key: Int?, bitmap: Bitmap) = bitmap.byteCount / 1024
        }
    }

    override fun requestImage(index: Int) {
        val xkcdPicInDb = XkcdModel.loadXkcdFromDB(index.toLong())
        if (xkcdPicInDb == null || xkcdPicInDb.targetImg.isNullOrBlank() || xkcdPicInDb.title.isNullOrBlank()) {
            XkcdModel.loadXkcd(index.toLong())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { view.setLoading(true) }
                    .subscribe({ xkcdPic ->
                        view.renderPic(xkcdPic.targetImg)
                        view.renderTitle(xkcdPic)
                    }, { e -> Timber.e(e, "Request pic in detail page error, %d", index) })
                    .also { compositeDisposable.add(it) }
        } else {
            view.renderPic(xkcdPicInDb.targetImg)
            view.renderTitle(xkcdPicInDb)
        }
    }

    override fun parseGifData(data: ByteArray?) {
        if (data == null || data.isEmpty()) {
            return
        }
        mMovie = Movie.decodeByteArray(data, 0, data.size)
        compositeDisposable.add(Observable.interval(100, TimeUnit.MILLISECONDS)
                .filter { mMovie != null }
                .take(1)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    duration = min(mMovie!!.duration(), Integer.MAX_VALUE) / step * ecoModeValue
                    view.renderSeekBar(duration)
                }, { Timber.e(it) }))
    }

    override fun parseFrame(progress: Int) {
        currentFrame = if (progress == 0) 1 else progress

        val bitmap = getBitmapFromMemCache(currentFrame)

        if (bitmap != null) {
            view.renderFrame(bitmap)
        } else {
            if (reusableBitmap == null || canvas == null) {
                reusableBitmap = Bitmap.createBitmap(movieWidth, movieHeight, Bitmap.Config.RGB_565)
                canvas = Canvas(reusableBitmap!!)
            }

            mMovie!!.setTime(currentFrame * step / ecoModeValue)
            mMovie!!.draw(canvas, 0f, 0f)
            addBitmapToMemoryCache(currentFrame, reusableBitmap!!)
            view.renderFrame(reusableBitmap!!)
        }
    }

    override fun adjustGifSpeed(increaseByOne: Int) {
        stepMultiplier += increaseByOne
        if (increaseByOne == 0) {
            stepMultiplier = 1
        }
        if (stepMultiplier == 0) {
            stepMultiplier = if (increaseByOne == 1) 1 else -1
        }
        stepMultiplier = stepMultiplier.coerceIn(-8, 8)
        view.showGifPlaySpeed(stepMultiplier)
    }

    override fun adjustGifFrame(isForward: Boolean) {
        var progress = if (isForward)
            currentFrame + ecoModeValue * stepMultiplier
        else
            currentFrame - ecoModeValue * stepMultiplier
        progress = progress.coerceIn(1, duration)
        progress = if (progress == 1 && isForward) 2 else progress
        view.changeGifSeekBarProgress(progress)
        parseFrame(progress)
        if (progress == 1 || progress == duration) {
            stepMultiplier = 1
        }
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
    }

    private fun addBitmapToMemoryCache(key: Int, bitmap: Bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap)
        }
    }

    private fun getBitmapFromMemCache(key: Int) = mMemoryCache.get(key)
}
