package xyz.jienan.xkcd.ui.xkcdimageview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import androidx.annotation.UiThread
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.ui.xkcdimageview.ImageInfoExtractor.TYPE_BITMAP
import xyz.jienan.xkcd.ui.xkcdimageview.ImageInfoExtractor.TYPE_STILL_IMAGE
import java.io.File

/**
 * Created by Piasy{github.com/Piasy} on 06/11/2016.
 * Modified by Charlie Zhang for xkcd project
 * <p>
 * Use FrameLayout for extensibility.
 */

class BigImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
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

    private var mProgressIndicatorView: View? = null
    private var mFailureImageView: ImageView? = null

//    private var mImageSaveCallback: ImageSaveCallback? = null
    private var mUserCallback: ImageLoader.Callback? = null
    var currentImageFile: File? = null
        private set
    private var mUri: Uri? = null
    private var mThumbnail: Uri? = null

    private var mOnClickListener: OnClickListener? = null
    private var mOnLongClickListener: OnLongClickListener? = null
    private val mFailureImageClickListener = object : OnClickListener {
        override fun onClick(v: View) {
            // Retry loading when failure image is clicked
            if (mTapToRetry) {
                showImage(mThumbnail, mUri)
            }
            if (mOnClickListener != null) {
                mOnClickListener!!.onClick(v)
            }
        }
    }

//    private var mProgressIndicator: ProgressIndicator? = null
//    private var mDisplayOptimizeListener: DisplayOptimizeListener? = null
    private var mInitScaleType: Int = 0
    private var mThumbnailScaleType: ScaleType? = null
    private var mFailureImageScaleType: ScaleType? = null
    private var mOptimizeDisplay: Boolean = false
    private var mTapToRetry: Boolean = false

    init {

        val array = context.theme
                .obtainStyledAttributes(attrs, R.styleable.BigImageView, defStyleAttr, 0)
        mInitScaleType = array.getInteger(R.styleable.BigImageView_initScaleType,
                INIT_SCALE_TYPE_FIT_CENTER)

        if (array.hasValue(R.styleable.BigImageView_failureImage)) {
            val scaleTypeIndex = array.getInteger(
                    R.styleable.BigImageView_failureImageInitScaleType,
                    DEFAULT_IMAGE_SCALE_TYPE)
            mFailureImageScaleType = scaleType(scaleTypeIndex)
            val mFailureImageDrawable = array.getDrawable(
                    R.styleable.BigImageView_failureImage)
            setFailureImage(mFailureImageDrawable)
        }
        if (array.hasValue(R.styleable.BigImageView_thumbnailScaleType)) {
            val scaleTypeIndex = array.getInteger(
                    R.styleable.BigImageView_thumbnailScaleType,
                    DEFAULT_IMAGE_SCALE_TYPE)
            mThumbnailScaleType = scaleType(scaleTypeIndex)
        }

        mOptimizeDisplay = array.getBoolean(R.styleable.BigImageView_optimizeDisplay, true)
        mTapToRetry = array.getBoolean(R.styleable.BigImageView_tapToRetry, true)

        array.recycle()

        if (isInEditMode) {
            mImageLoader = null
        } else {
            mImageLoader = ImageLoaderFactory.imageLoader
        }
        mInternalCallback = ThreadedCallbacks.create(ImageLoader.Callback::class.java, this)

        mViewFactory = ImageViewFactory()

        mTempImages = ArrayList()
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

    fun setImageViewFactory(viewFactory: ImageViewFactory?) {
        if (viewFactory == null) {
            return
        }

        mViewFactory = viewFactory
    }

    fun setFailureImageInitScaleType(scaleType: ScaleType) {
        mFailureImageScaleType = scaleType
    }

    fun setFailureImage(failureImage: Drawable?) {
        // Failure image is not set
        if (failureImage == null) {
            return
        }

        if (mFailureImageView == null) {
            // Init failure image
            mFailureImageView = ImageView(context)
            mFailureImageView!!.setVisibility(GONE)
            mFailureImageView!!.setOnClickListener(mFailureImageClickListener)

            if (mFailureImageScaleType != null) {
                mFailureImageView!!.setScaleType(mFailureImageScaleType)
            }

            addView(mFailureImageView)
        }

        mFailureImageView!!.setImageDrawable(failureImage)
    }

    fun setInitScaleType(initScaleType: Int) {
        if (ssiv == null) {
            return
        }

        mInitScaleType = initScaleType
        when (initScaleType) {
            INIT_SCALE_TYPE_CENTER_CROP -> ssiv!!.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP)
            INIT_SCALE_TYPE_CUSTOM -> ssiv!!.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM)
            INIT_SCALE_TYPE_START -> ssiv!!.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START)
            INIT_SCALE_TYPE_CENTER_INSIDE -> ssiv!!.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE)
            else -> ssiv!!.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE)
        }
//        if (mDisplayOptimizeListener != null) {
//            mDisplayOptimizeListener!!.setInitScaleType(initScaleType)
//        }
    }

    fun setThumbnailScaleType(scaleType: ScaleType) {
        mThumbnailScaleType = scaleType
    }

    fun setOptimizeDisplay(optimizeDisplay: Boolean) {
        if (ssiv == null) {
            return
        }

        mOptimizeDisplay = optimizeDisplay
        if (mOptimizeDisplay) {
//            mDisplayOptimizeListener = DisplayOptimizeListener(ssiv)
//            ssiv!!.setOnImageEventListener(mDisplayOptimizeListener)
        } else {
//            mDisplayOptimizeListener = null
            ssiv!!.setOnImageEventListener(null)
        }
    }

    fun setTapToRetry(tapToRetry: Boolean) {
        mTapToRetry = tapToRetry
    }

//    fun setImageSaveCallback(imageSaveCallback: ImageSaveCallback) {
//        mImageSaveCallback = imageSaveCallback
//    }
//
//    fun setProgressIndicator(progressIndicator: ProgressIndicator) {
//        mProgressIndicator = progressIndicator
//    }

    fun setImageLoaderCallback(imageLoaderCallback: ImageLoader.Callback) {
        mUserCallback = imageLoaderCallback
    }

//    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//    fun saveImageIntoGallery() {
//        if (currentImageFile == null) {
//            if (mImageSaveCallback != null) {
//                mImageSaveCallback!!.onFail(IllegalStateException("image not downloaded yet"))
//            }
//
//            return
//        }
//
//        try {
//            val result = MediaStore.Images.Media.insertImage(context.contentResolver,
//                    currentImageFile!!.absolutePath, currentImageFile!!.name, "")
//            if (mImageSaveCallback != null) {
//                if (!TextUtils.isEmpty(result)) {
//                    mImageSaveCallback!!.onSuccess(result)
//                } else {
//                    mImageSaveCallback!!.onFail(RuntimeException("saveImageIntoGallery fail"))
//                }
//            }
//        } catch (e: FileNotFoundException) {
//            if (mImageSaveCallback != null) {
//                mImageSaveCallback!!.onFail(e)
//            }
//        }
//
//    }

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

    fun showImage(thumbnail: Uri?, uri: Uri?) {
        mThumbnail = thumbnail
        mUri = uri

        clearThumbnailAndProgressIndicator()
        mImageLoader!!.loadImage(hashCode(), uri!!, mInternalCallback)

        if (mFailureImageView != null) {
            mFailureImageView!!.visibility = GONE
        }
    }

    fun cancel() {
        mImageLoader!!.cancel(hashCode())
    }

    override fun onCacheHit(imageType: Int, image: File) {
        currentImageFile = image
        doShowImage(imageType, image)

        if (mUserCallback != null) {
            mUserCallback!!.onCacheHit(imageType, image)
        }
    }

    override fun onCacheMiss(imageType: Int, image: File) {
        currentImageFile = image
        mTempImages.add(image)
        doShowImage(imageType, image)

        if (mUserCallback != null) {
            mUserCallback!!.onCacheMiss(imageType, image)
        }
    }

    override fun onStart() {
        // why show thumbnail in onStart? because we may not need download it from internet
        if (mThumbnail !== Uri.EMPTY) {
            mThumbnailView = mViewFactory!!.createThumbnailView(context, mThumbnail!!,
                    mThumbnailScaleType!!)
            if (mThumbnailView != null) {
                addView(mThumbnailView, ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
            }
        }

//        if (mProgressIndicator != null) {
//            mProgressIndicatorView = mProgressIndicator!!.getView(this@BigImageView)
//            mProgressIndicator!!.onStart()
//            if (mProgressIndicatorView != null) {
//                addView(mProgressIndicatorView)
//            }
//        }

        if (mUserCallback != null) {
            mUserCallback!!.onStart()
        }
    }

    override fun onProgress(progress: Int) {
//        if (mProgressIndicator != null) {
//            mProgressIndicator!!.onProgress(progress)
//        }
        if (mUserCallback != null) {
            mUserCallback!!.onProgress(progress)
        }
    }

    override fun onFinish() {
        doOnFinish()
        if (mUserCallback != null) {
            mUserCallback!!.onFinish()
        }
    }

    override fun onSuccess(image: File) {
        if (mUserCallback != null) {
            mUserCallback!!.onSuccess(image)
        }
    }

    override fun onFail(error: Exception) {
        showFailImage()

        if (mUserCallback != null) {
            mUserCallback!!.onFail(error)
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
            if (mProgressIndicatorView != null) {
                mProgressIndicatorView!!.animation = set
            }

//            if (mProgressIndicator != null) {
//                mProgressIndicator!!.onFinish()
//            }

            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    if (mThumbnailView != null) {
                        mThumbnailView!!.setVisibility(GONE)
                    }
                    if (mProgressIndicatorView != null) {
                        mProgressIndicatorView!!.setVisibility(GONE)
                    }

                    // fix:
                    // java.lang.NullPointerException:
                    // Attempt to read from field 'int android.view.View.mViewFlags'
                    // on a null object reference
                    // ref: https://stackoverflow.com/q/33242776/3077508
                    if (mThumbnailView != null || mProgressIndicatorView != null) {
                        post { clearThumbnailAndProgressIndicator() }
                    }
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
        } else {
//            if (mProgressIndicator != null) {
//                mProgressIndicator!!.onFinish()
//            }
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

            setOptimizeDisplay(mOptimizeDisplay)
            setInitScaleType(mInitScaleType)

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

    @UiThread
    private fun showFailImage() {
        // Failure image is not set
        if (mFailureImageView == null) {
            return
        }
        if (mMainView != null) {
            removeView(mMainView)
        }

        mFailureImageView!!.visibility = View.VISIBLE
        clearThumbnailAndProgressIndicator()
    }

    private fun clearThumbnailAndProgressIndicator() {
        if (mThumbnailView != null) {
            removeView(mThumbnailView)
            mThumbnailView = null
        }
        if (mProgressIndicatorView != null) {
            removeView(mProgressIndicatorView)
            mProgressIndicatorView = null
        }
    }

    companion object {
        val INIT_SCALE_TYPE_CENTER = 0
        val INIT_SCALE_TYPE_CENTER_CROP = 1
        val INIT_SCALE_TYPE_CENTER_INSIDE = 2
        val INIT_SCALE_TYPE_FIT_CENTER = 3
        val INIT_SCALE_TYPE_FIT_END = 4
        val INIT_SCALE_TYPE_FIT_START = 5
        val INIT_SCALE_TYPE_FIT_XY = 6
        val INIT_SCALE_TYPE_CUSTOM = 7
        val INIT_SCALE_TYPE_START = 8

        val DEFAULT_IMAGE_SCALE_TYPE = 3
        val IMAGE_SCALE_TYPES = arrayOf<ScaleType>(ScaleType.CENTER, ScaleType.CENTER_CROP, ScaleType.CENTER_INSIDE, ScaleType.FIT_CENTER, ScaleType.FIT_END, ScaleType.FIT_START, ScaleType.FIT_XY)

        fun scaleType(value: Int): ScaleType {
            return if (0 <= value && value < IMAGE_SCALE_TYPES.size) {
                IMAGE_SCALE_TYPES[value]
            } else IMAGE_SCALE_TYPES[DEFAULT_IMAGE_SCALE_TYPE]
        }
    }
}