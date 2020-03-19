package xyz.jienan.xkcd.comics.activity

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.SeekBar
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposables
import kotlinx.android.synthetic.main.activity_image_detail.*
import timber.log.Timber
import xyz.jienan.xkcd.Const.*
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.base.BaseActivity
import xyz.jienan.xkcd.base.glide.XkcdGlideUtils
import xyz.jienan.xkcd.base.glide.fallback
import xyz.jienan.xkcd.comics.contract.ImageDetailPageContract
import xyz.jienan.xkcd.comics.presenter.ImageDetailPagePresenter
import xyz.jienan.xkcd.model.XkcdPic
import xyz.jienan.xkcd.ui.AnimUtils
import xyz.jienan.xkcd.ui.ToastUtils
import xyz.jienan.xkcd.ui.xkcdimageview.ImageLoader
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Created by jienanzhang on 21/05/2019.
 */

class ImageDetailPageActivity : BaseActivity(), ImageDetailPageContract.View {

    private val listener = View.OnClickListener {
        finish()
        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
    }

    private val index by lazy { intent.getLongExtra(KEY_ID, 0L).toInt() }

    private val compositeDisposable = CompositeDisposable()

    private var holdDisposable = Disposables.disposed()

    private val imageDetailPagePresenter: ImageDetailPageContract.Presenter by lazy { ImageDetailPagePresenter(this) }

    private val showTitle by lazy { intent.getBooleanExtra(KEY_SHOW_TITLE, false) }

    private val glide by lazy { Glide.with(this) }

    private val titlePx by lazy { resources.getDimension(R.dimen.top_title_padding).toInt() }

    private var url: String? = ""

    private val isEcoMode by lazy { sharedPreferences.getBoolean(PREF_XKCD_GIF_ECO, true) }

    private var isGifInPlayState: Boolean
        get() = playBtn!!.tag.toString() != "0"
        set(isPlay) {
            playBtn!!.tag = if (isPlay) "1" else "0"
            if (!isPlay) {
                stopPlayingGif()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageDetailPagePresenter.isEcoMode = isEcoMode
        setContentView(R.layout.activity_image_detail)
        url = intent.getStringExtra(KEY_URL)
        if (!url.isNullOrBlank()) {
            renderPic(url!!)
        } else if (index != 0) {
            imageDetailPagePresenter.requestImage(index)
        } else {
            Timber.e("No valid info for detail page")
            finish()
        }

        bigImageView?.ssiv?.maxScale = MAX_SCALE.toFloat()
        bigImageView?.ssiv?.setOnStateChangedListener(object : SubsamplingScaleImageView.DefaultOnStateChangedListener() {

            private var initScale = 0.0f

            override fun onScaleChanged(newScale: Float, origin: Int) {
                if (showTitle) {
                    if (initScale == 0f) {
                        initScale = newScale
                    }
                    tvTitle!!.visibility = if (newScale - initScale < 0.16f) View.VISIBLE else View.GONE
                }
                isGifInPlayState = false
            }
        })
        btnGifBack.setOnClickListener { onGifSpeedClicked(it) }
        btnGifForward.setOnClickListener { onGifSpeedClicked(it) }
        btnGifBack.setOnTouchListener { v, event -> onGifSpeedPressed(v, event) }
        btnGifForward.setOnTouchListener { v, event -> onGifSpeedPressed(v, event) }
        playBtn.setOnClickListener { onGifPlayClicked() }
        bigImageView.onExitListener = {
            finish()
            overridePendingTransition(R.anim.fadein, R.anim.fadeout_drop)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            bigImageView.setOnApplyWindowInsetsListener { _, windowInsets ->
                adjustContentForCutouts()
                windowInsets
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.P)
    private fun adjustContentForCutouts() {
        window.decorView.rootWindowInsets?.displayCutout?.apply {
            tvTitle.apply {
                setPadding(titlePx + safeInsetLeft, titlePx, titlePx + safeInsetRight, titlePx)
                layoutParams = (layoutParams as RelativeLayout.LayoutParams).apply { topMargin = safeInsetTop }
            }
            bigImageView.apply { setPadding(safeInsetLeft, safeInsetTop, safeInsetRight, safeInsetBottom) }
            pbLoading.apply { setPadding(safeInsetLeft, safeInsetTop, safeInsetRight, safeInsetBottom) }
            gifPanel.apply { setPadding(safeInsetLeft, safeInsetTop, safeInsetRight, safeInsetBottom) }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        gifPanel.visibility = View.GONE
        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
    }

    override fun onDestroy() {
        imageDetailPagePresenter.onDestroy()
        isGifInPlayState = false
        compositeDisposable.dispose()
        super.onDestroy()
    }

    override fun renderPic(url: String) {
        this.url = url

        if (url.endsWith("gif")) {
            loadGifWithControl()
        } else {
            loadImgWithoutControl(url)
        }

        logUXEvent(FIRE_DETAIL_PAGE, bundleOf(FIRE_COMIC_ID to index, FIRE_COMIC_URL to url))

        Observable.timer(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    bigImageView!!.setOnClickListener(listener)
                }, { e -> Timber.e(e, "add listener error") })
                .let { compositeDisposable.add(it) }
    }

    override fun renderTitle(xkcdPic: XkcdPic) {
        if (showTitle) {
            tvTitle!!.text = getString(R.string.item_search_title, xkcdPic.num.toString(), xkcdPic.title)
            tvTitle!!.visibility = View.VISIBLE
        }
    }

    override fun renderSeekBar(duration: Int) {
        pbLoading!!.visibility = View.GONE
        gifPanel!!.visibility = View.VISIBLE
        sbMovie!!.visibility = View.VISIBLE
        sbMovie!!.max = duration
        sbMovie!!.setOnSeekBarChangeListener(GifSeekBarListener())
        imageDetailPagePresenter.parseFrame(1)
        onGifPlayClicked()
    }

    override fun onPause() {
        if (isGifInPlayState) {
            onGifPlayClicked()
        }
        super.onPause()
    }

    override fun renderFrame(bitmap: Bitmap) {
        bigImageView.showBitmap(bitmap)
        sbMovie!!.thumb = BitmapDrawable(resources,
                Bitmap.createScaledBitmap(bitmap, 100, 100, false))
    }

    override fun changeGifSeekBarProgress(progress: Int) {
        sbMovie!!.progress = progress
    }

    override fun showGifPlaySpeed(speed: Int) {
        if (isGifInPlayState) {
            ToastUtils.showToast(this, String.format(if (speed < 0) "<< %d" else "%d >>", speed), position = Gravity.CENTER)
        } else {
            ToastUtils.cancelToast()
        }
    }

    override fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            pbLoading!!.visibility = View.VISIBLE
        } else {
            pbLoading!!.visibility = View.GONE
        }
    }

    private fun onGifSpeedClicked(view: View) {
        val isForward = view.id == R.id.btnGifForward
        if (isGifInPlayState) {
            imageDetailPagePresenter.adjustGifSpeed(if (isForward) 1 else -1)
            logUXEvent(if (isForward) FIRE_GIF_FAST_FORWARD else FIRE_GIF_FAST_REWIND)
        } else {
            imageDetailPagePresenter.adjustGifSpeed(0)
            imageDetailPagePresenter.adjustGifFrame(isForward)
            logUXEvent(if (isForward) FIRE_GIF_NEXT_CLICK else FIRE_GIF_PREVIOUS_CLICK)
        }
        AnimUtils.vectorAnim(view as ImageView?, if (isForward) R.drawable.anim_fast_forward_shake else R.drawable.anim_fast_rewind_shake)
    }

    private fun onGifSpeedPressed(view: View, motionEvent: MotionEvent): Boolean {
        if (!isGifInPlayState) {
            if (motionEvent.action == MotionEvent.ACTION_CANCEL || motionEvent.action == MotionEvent.ACTION_UP) {
                stopPlayingGif()
            } else if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                startPlayingGif(view.id == R.id.btnGifForward, true)
            }
        }
        return false
    }

    private fun onGifPlayClicked() {
        isGifInPlayState = !isGifInPlayState
        stopPlayingGif()
        if (isGifInPlayState) {
            startPlayingGif(isForward = true, isFromUserLongPress = false)
        }
    }

    private fun startPlayingGif(isForward: Boolean, isFromUserLongPress: Boolean) {
        stopPlayingGif()
        holdDisposable = Observable.interval((if (isEcoMode) 100 else 60).toLong(), TimeUnit.MILLISECONDS)
                .delay((if (isFromUserLongPress) 500 else 0).toLong(), TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    if (!isFromUserLongPress) {
                        AnimUtils.vectorAnim(playBtn, R.drawable.anim_play_to_pause, R.drawable.ic_pause)
                    }
                }
                .doOnDispose {
                    runOnUiThread {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        imageDetailPagePresenter.adjustGifSpeed(0)
                        if (!isFromUserLongPress) {
                            AnimUtils.vectorAnim(playBtn, R.drawable.anim_pause_to_play, R.drawable.ic_play_arrow)
                        }
                    }
                }
                .doOnNext { num ->
                    if (num == 10L && isFromUserLongPress) {
                        logUXEvent(if (isForward) FIRE_GIF_NEXT_HOLD else FIRE_GIF_PREVIOUS_HOLD)
                    }
                }
                .subscribe({
                    if (!isFromUserLongPress && sbMovie!!.progress == sbMovie!!.max && isForward) {
                        imageDetailPagePresenter.parseFrame(1)
                    }
                    imageDetailPagePresenter.adjustGifFrame(isForward)
                }, { Timber.e(it, "Failed to play gif") })
                .also { compositeDisposable.add(it) }
    }

    private fun stopPlayingGif() {
        if (!holdDisposable.isDisposed) {
            holdDisposable.dispose()
        }
    }

    private fun loadGifWithControl() {
        pbLoading!!.visibility = View.VISIBLE
        XkcdGlideUtils.loadGif(glide!!, url!!, object : SimpleTarget<GifDrawable>() {

            override fun onResourceReady(resource: GifDrawable, glideAnimation: GlideAnimation<in GifDrawable>) {
                imageDetailPagePresenter.parseGifData(resource.data)
            }
        })
    }

    private fun loadImgWithoutControl(url: String) {
        pbLoading!!.visibility = View.VISIBLE
        bigImageView.showImage(Uri.parse(url))
        bigImageView.setImageLoaderCallback(object : ImageLoader.Callback {
            override fun onFinish() {
                Timber.d("")
            }

            override fun onSuccess(image: File) {
                val ssiv = bigImageView.ssiv ?: return
                ssiv.setOnImageEventListener(object : SubsamplingScaleImageView.OnImageEventListener {
                    override fun onImageLoaded() {
                        Timber.d("")
                    }

                    override fun onReady() {

                        pbLoading!!.visibility = View.GONE
                        ssiv.setDoubleTapZoomDuration(200)
                        Timber.d("")
                        var result = 0.5f
                        val imageWidth = ssiv.sWidth
                        val imageHeight = ssiv.sHeight
                        val viewWidth = ssiv.width
                        val viewHeight = ssiv.height

                        var hasZeroValue = false
                        if (imageWidth == 0 || imageHeight == 0 || viewWidth == 0 || viewHeight == 0) {
                            result = 0.5f
                            hasZeroValue = true
                        }

                        val viewWHRatio = viewWidth / viewHeight.toFloat()
                        val imageWHRatio = imageWidth / imageHeight.toFloat()

                        if (!hasZeroValue) {
                            result = if (imageWHRatio <= viewWHRatio) {
                                viewWidth / imageWidth.toFloat()
                            } else {
                                viewHeight / imageHeight.toFloat()
                            }
                        }

                        val maxScale = (viewWidth / imageWidth.toFloat())
                                .coerceAtLeast(viewHeight / imageHeight.toFloat())
                        if (maxScale > 1) {
                            // image is smaller than screen, it should be zoomed out to its origin size
                            ssiv.minScale = 1f

                            // and it should be zoomed in to fill the screen
                            val defaultMaxScale = ssiv.maxScale
                            ssiv.maxScale = defaultMaxScale.coerceAtLeast(maxScale * 1.2F)

                            val fitScreenRatio = viewWHRatio / imageWHRatio

                            if (fitScreenRatio < 1.2 && fitScreenRatio > 0.9) {
                                result *= 2
                            }
                        } else {
                            // image is bigger than screen, it should be zoomed out to fit the screen
                            val minScale = (viewWidth / imageWidth.toFloat())
                                    .coerceAtMost(viewHeight / imageHeight.toFloat())
                            ssiv.minScale = minScale
                            // but no need to set max scale
                        }
                        // scale to fit screen, and center
                        ssiv.setDoubleTapZoomScale(result)
                        ssiv.resetScaleAndCenter()
                    }

                    override fun onTileLoadError(e: Exception?) {
                        Timber.d("")
                    }

                    override fun onPreviewReleased() {
                        Timber.d("")
                    }

                    override fun onImageLoadError(e: Exception?) {
                        Timber.d("")
                    }

                    override fun onPreviewLoadError(e: Exception?) {
                        Timber.d("")
                    }
                })
            }

            override fun onFail(error: Exception) {
                val fallback = url.fallback()
                if (fallback != url) {
                    loadImgWithoutControl(fallback)
                }
            }

            override fun onCacheHit(imageType: Int, image: File) {
                Timber.d("")
            }

            override fun onCacheMiss(imageType: Int, image: File) {
                Timber.d("")
            }

            override fun onProgress(progress: Int) {
                Timber.d("")
            }

            override fun onStart() {
                Timber.d("")
            }
        })
    }

    private inner class GifSeekBarListener : SeekBar.OnSeekBarChangeListener {

        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                imageDetailPagePresenter.parseFrame(progress)
                isGifInPlayState = false
            } else {
                if (seekBar.progress == 1 || seekBar.progress == seekBar.max) {
                    isGifInPlayState = false
                }
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            // no ops
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            logUXEvent(FIRE_GIF_USER_PROGRESS)
        }
    }

    companion object {

        private const val KEY_URL = "URL"

        private const val KEY_ID = "ID"

        private const val KEY_SHOW_TITLE = "show_title"

        private const val MAX_SCALE = 10

        @JvmStatic
        fun startActivity(context: Context,
                          url: String?,
                          id: Long) {
            if (id <= 0) {
                return
            }
            val intent = Intent(context, ImageDetailPageActivity::class.java)
            intent.putExtra(KEY_URL, url)
            intent.putExtra(KEY_ID, id)
            context.startActivity(intent)
        }

        @JvmStatic
        fun startActivityFromId(context: Context, id: Long) {
            if (id <= 0) {
                return
            }
            val intent = Intent(context, ImageDetailPageActivity::class.java)
            intent.putExtra(KEY_ID, id)
            intent.putExtra(KEY_SHOW_TITLE, true)
            context.startActivity(intent)
        }
    }
}
