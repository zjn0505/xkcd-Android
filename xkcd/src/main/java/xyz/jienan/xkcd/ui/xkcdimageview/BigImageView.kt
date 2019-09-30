package xyz.jienan.xkcd.ui.xkcdimageview

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.UiThread
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.ui.xkcdimageview.ImageInfoExtractor.TYPE_BITMAP
import java.io.File

/**
 * Created by Piasy{github.com/Piasy} on 06/11/2016.
 * Modified by Charlie Zhang for xkcd project
 * <p>
 * Use FrameLayout for extensibility.
 */

open class BigImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        FrameLayout(context, attrs, defStyleAttr),
        ImageLoader.Callback {

    private val mImageLoader: ImageLoader?

    private val mTempImages: MutableList<File>

    private val mInternalCallback: ImageLoader.Callback

    private var mViewFactory: ImageViewFactory? = null

    private var mMainView: View? = null

    private var mThumbnailView: View? = null

    var ssiv: SubsamplingScaleImageView? = null
        private set

    private var mFailureImageView: ImageView? = null

    private var mUserCallback: ImageLoader.Callback? = null

    private var currentImageFile: File? = null

    private var mUri: Uri? = null

    private var mThumbnail: Uri? = null

    private var mOnClickListener: OnClickListener? = null

    private var mOnLongClickListener: OnLongClickListener? = null

    private var mInitScaleType: Int = 0

    private var mOptimizeDisplay: Boolean = false

    init {

        val array = context.theme
                .obtainStyledAttributes(attrs, R.styleable.BigImageView, defStyleAttr, 0)
        mInitScaleType = array.getInteger(R.styleable.BigImageView_initScaleType,
                INIT_SCALE_TYPE_FIT_CENTER)

        array.recycle()

        mImageLoader = if (isInEditMode) {
            null
        } else {
            ImageLoaderFactory.imageLoader
        }
        mInternalCallback = this

        mViewFactory = ImageViewFactory()

        mTempImages = arrayListOf()
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        mOnClickListener = listener
        if (mMainView != null) {
            mMainView!!.setOnClickListener(listener)
        }
    }

    override fun setOnLongClickListener(listener: OnLongClickListener?) {
        mOnLongClickListener = listener
        if (mMainView != null) {
            mMainView!!.setOnLongClickListener(listener)
        }
    }

    fun setImageLoaderCallback(imageLoaderCallback: ImageLoader.Callback) {
        mUserCallback = imageLoaderCallback
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        mImageLoader!!.cancel(hashCode())

        var i = 0
        val size = mTempImages.size
        while (i < size) {
            mTempImages[i].delete()
            i++
        }
        mTempImages.clear()
    }

    fun showBitmap(bitmap: Bitmap) {
        doShowImage(TYPE_BITMAP, null, bitmap)
    }

    fun showImage(uri: Uri) {
        showImage(Uri.EMPTY, uri)
    }

    private fun showImage(thumbnail: Uri?, uri: Uri?) {
        mThumbnail = thumbnail
        mUri = uri

        clearThumbnailAndProgressIndicator()
        mImageLoader!!.loadImage(hashCode(), uri!!, mInternalCallback)

        if (mFailureImageView != null) {
            mFailureImageView!!.visibility = GONE
        }
    }

    override fun onCacheHit(imageType: Int, image: File) {
        post {
            currentImageFile = image
            doShowImage(imageType, image)

            if (mUserCallback != null) {
                mUserCallback!!.onCacheHit(imageType, image)
            }
        }
    }

    override fun onCacheMiss(imageType: Int, image: File) {
        post {
            currentImageFile = image
            mTempImages.add(image)
            doShowImage(imageType, image)

            if (mUserCallback != null) {
                mUserCallback!!.onCacheMiss(imageType, image)
            }
        }
    }

    override fun onStart() {
        post {
            if (mUserCallback != null) {
                mUserCallback!!.onStart()
            }
        }
    }

    override fun onProgress(progress: Int) {
        post {
            if (mUserCallback != null) {
                mUserCallback!!.onProgress(progress)
            }
        }
    }

    override fun onFinish() {
        post {
            doOnFinish()
            if (mUserCallback != null) {
                mUserCallback!!.onFinish()
            }
        }
    }

    override fun onSuccess(image: File) {
        post {
            if (mUserCallback != null) {
                mUserCallback!!.onSuccess(image)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (isOriginalSized) {
            ssiv?.postDelayed({ ssiv?.resetScaleAndCenter() }, 100)
        }
    }

    override fun onFail(error: Exception) {
        post {
            if (mUserCallback != null) {
                mUserCallback!!.onFail(error)
            }
        }
    }

    @UiThread
    private fun doOnFinish() {
        if (mOptimizeDisplay) {
            val set = AnimationSet(true)
            val animation = AlphaAnimation(1f, 0f)
            animation.duration = 500
            animation.fillAfter = true
            set.addAnimation(animation)
            if (mThumbnailView != null) {
                mThumbnailView!!.animation = set
            }

            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    if (mThumbnailView != null) {
                        mThumbnailView!!.visibility = GONE
                    }
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
        } else {
            clearThumbnailAndProgressIndicator()
        }
    }

    @UiThread
    private fun doShowImage(imageType: Int, image: File?, bitmap: Bitmap? = null) {
        if (mMainView != null) {
            removeView(mMainView)
        }

        mMainView = mViewFactory!!.createMainView(context, imageType, image, mInitScaleType)
        if (mMainView == null) {
            onFail(RuntimeException("Image type not supported: " + ImageInfoExtractor.typeName(imageType)))
            return
        }

        addView(mMainView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        mMainView!!.setOnClickListener(mOnClickListener)
        mMainView!!.setOnLongClickListener(mOnLongClickListener)

        if (mMainView is SubsamplingScaleImageView) {
            ssiv = mMainView as SubsamplingScaleImageView

            ssiv!!.setMinimumTileDpi(160)

            if (image != null) {
                ssiv!!.setImage(ImageSource.uri(Uri.fromFile(image)))
            } else if (bitmap != null) {
                ssiv!!.setImage(ImageSource.bitmap(bitmap))
            }
        }

        if (mFailureImageView != null) {
            mFailureImageView!!.visibility = GONE
        }
    }

    protected val isOriginalSized: Boolean
        get() = (ssiv?.scale ?: 1f) / (ssiv?.minScale ?: 1f) < MIN_ORIGINAL_DIFF

    private fun clearThumbnailAndProgressIndicator() {
        if (mThumbnailView != null) {
            removeView(mThumbnailView)
            mThumbnailView = null
        }
    }

    companion object {
        const val INIT_SCALE_TYPE_FIT_CENTER = 3
        private const val MIN_ORIGINAL_DIFF = 1.09f
    }
}