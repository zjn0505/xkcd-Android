package xyz.jienan.xkcd.ui.xkcdimageview

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import java.io.File


/**
 * Created by Piasy{github.com/Piasy} on 2018/8/12.
 */
open class ImageViewFactory {

    fun createMainView(context: Context, imageType: Int, imageFile: File?,
                       initScaleType: Int): View? {
        return when (imageType) {
            ImageInfoExtractor.TYPE_GIF, ImageInfoExtractor.TYPE_ANIMATED_WEBP -> createAnimatedImageView(context, imageType, imageFile, initScaleType)
            ImageInfoExtractor.TYPE_STILL_WEBP, ImageInfoExtractor.TYPE_STILL_IMAGE -> createStillImageView(context)
            ImageInfoExtractor.TYPE_BITMAP -> createStillImageView(context)
            else -> createStillImageView(context)
        }
    }

    open fun createStillImageView(context: Context): SubsamplingScaleImageView {
        return SubsamplingScaleImageView(context)
    }

    open fun createAnimatedImageView(context: Context, imageType: Int, imageFile: File?,
                                               initScaleType: Int): View? {
        return null
    }

    open fun createThumbnailView(context: Context, thumbnail: Uri, scaleType: ImageView.ScaleType): View? {
        return null
    }
}