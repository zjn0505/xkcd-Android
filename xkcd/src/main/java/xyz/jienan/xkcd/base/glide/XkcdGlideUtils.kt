package xyz.jienan.xkcd.base.glide

import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import xyz.jienan.xkcd.model.XkcdPic
import xyz.jienan.xkcd.model.util.XkcdSideloadUtils.getPicFromXkcd
import xyz.jienan.xkcd.model.util.XkcdSideloadUtils.isSpecialComics

object XkcdGlideUtils {
    fun load(glide: RequestManager, pic: XkcdPic, url: String, imageView: ImageView?) {
        glide.load(url)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .priority(Priority.HIGH)
                .fitCenter()
                .dontTransform()
                .listener(object : RequestListener<String, Bitmap?> {
                    override fun onException(e: Exception, model: String, target: Target<Bitmap?>, isFirstResource: Boolean): Boolean {
                        if (model.isEmpty()) {
                            return false
                        } else if (model.startsWith("https")) {
                            load(glide, pic, model.replaceFirst("https".toRegex(), "http"), imageView)
                            return true
                        } else if (model.replaceFirst("http".toRegex(), "https") == pic.targetImg && isSpecialComics(pic)) {
                            load(glide, pic, getPicFromXkcd(pic).img, imageView)
                            return true
                        }
                        return false
                    }

                    override fun onResourceReady(resource: Bitmap?, model: String,
                                                 target: Target<Bitmap?>, isFromMemoryCache: Boolean,
                                                 isFirstResource: Boolean): Boolean {
                        return false
                    }
                })
                .into(imageView)
    }

    fun load(glide: RequestManager, url: String, imageView: ImageView?) {
        glide.load(url)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(object : RequestListener<String, GlideDrawable?> {
                    override fun onException(e: Exception, model: String,
                                             target: Target<GlideDrawable?>, isFirstResource: Boolean): Boolean {
                        val fallback = model.fallback()

                        return if (fallback != model) {
                            load(glide, fallback, imageView)
                            true
                        } else {
                            false
                        }
                    }

                    override fun onResourceReady(resource: GlideDrawable?,
                                                 model: String,
                                                 target: Target<GlideDrawable?>,
                                                 isFromMemoryCache: Boolean,
                                                 isFirstResource: Boolean): Boolean {
                        return false
                    }
                }).into(imageView)
    }

    fun loadGif(glide: RequestManager, url: String, target: Target<GifDrawable?>?) {
        glide.load(url)
                .asGif()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(object : RequestListener<String, GifDrawable?> {
                    override fun onException(e: Exception, model: String,
                                             target: Target<GifDrawable?>, isFirstResource: Boolean): Boolean {

                        val fallback = model.fallback()

                        return if (fallback != model) {
                            loadGif(glide, fallback, target)
                            true
                        } else {
                            false
                        }
                    }

                    override fun onResourceReady(resource: GifDrawable?,
                                                 model: String,
                                                 target: Target<GifDrawable?>, isFromMemoryCache: Boolean,
                                                 isFirstResource: Boolean): Boolean {
                        return false
                    }
                })
                .into(target)
    }
}