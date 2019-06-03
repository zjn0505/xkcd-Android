package xyz.jienan.xkcd.base.glide

import android.graphics.drawable.Drawable

import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget

import java.io.File

/**
 * Created by Piasy{github.com/Piasy} on 12/11/2016.
 */

abstract class ImageDownloadTarget protected constructor(private val mUrl: String) : SimpleTarget<File>(), OkHttpProgressGlideModule.UIProgressListener {

    override fun onResourceReady(resource: File, glideAnimation: GlideAnimation<in File>) {
        OkHttpProgressGlideModule.forget(mUrl)
    }

    override fun onLoadCleared(placeholder: Drawable?) {
        super.onLoadCleared(placeholder)
        OkHttpProgressGlideModule.forget(mUrl)
    }

    override fun onLoadStarted(placeholder: Drawable?) {
        super.onLoadStarted(placeholder)
        OkHttpProgressGlideModule.expect(mUrl, this)
    }

    override fun onLoadFailed(e: Exception?, errorDrawable: Drawable?) {
        super.onLoadFailed(e, errorDrawable)
        OkHttpProgressGlideModule.forget(mUrl)
    }

}
