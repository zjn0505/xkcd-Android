/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Piasy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package xyz.jienan.xkcd.base.glide

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import xyz.jienan.xkcd.ui.xkcdimageview.ImageInfoExtractor
import xyz.jienan.xkcd.ui.xkcdimageview.ImageLoader
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by Piasy{github.com/Piasy} on 09/11/2016.
 */

class GlideImageLoader private constructor(private val context: Context) : ImageLoader {
    private val mRequestTargetMap = ConcurrentHashMap<Int, ImageDownloadTarget>()

    override fun loadImage(requestId: Int, uri: Uri, callback: ImageLoader.Callback) {
        val target = object : ImageDownloadTarget(uri.toString()) {

            override fun onResourceReady(resource: File, glideAnimation: GlideAnimation<in File>) {
                super.onResourceReady(resource, glideAnimation)
                // we don't need delete this image file, so it behaves live cache hit
                callback.onCacheHit(ImageInfoExtractor.getImageType(resource), resource)
                callback.onSuccess(resource)
            }

            override fun onLoadFailed(e: Exception?, errorDrawable: Drawable?) {
                super.onLoadFailed(e, errorDrawable)
                if (uri.path != null && uri.path!!.startsWith("https")) {
                    Glide.with(context).load(Uri.parse(uri.path!!.replaceFirst("https".toRegex(), "http")))
                            .downloadOnly<ImageDownloadTarget>(this)
                    return
                }
                callback.onFail(GlideLoaderException(errorDrawable))
            }

            override fun onProgress(bytesRead: Long, expectedLength: Long) {
                val progress = (bytesRead.toFloat() / expectedLength * 100).toInt()
                callback.onProgress(progress)
            }

            override fun getGranualityPercentage() = 0f

            override fun onDownloadStart() {
                callback.onStart()
            }

            override fun onProgress(progress: Int) {
                callback.onProgress(progress)
            }

            override fun onDownloadFinish() {
                callback.onFinish()
            }
        }
        clearTarget(requestId)
        saveTarget(requestId, target)

        Glide.with(context)
                .load(uri)
                .downloadOnly<ImageDownloadTarget>(target)
    }

    private fun saveTarget(requestId: Int, target: ImageDownloadTarget) {
        mRequestTargetMap[requestId] = target
    }

    private fun clearTarget(requestId: Int) {
        val target = mRequestTargetMap.remove(requestId)
        if (target != null) {
            Glide.clear(target)
        }
    }

    override fun prefetch(uri: Uri) {
        Glide.with(context)
                .load(uri)
                .downloadOnly(object : SimpleTarget<File>() {
                    override fun onResourceReady(resource: File, glideAnimation: GlideAnimation<in File>) {

                    }
                })
    }

    override fun cancel(requestId: Int) {
        clearTarget(requestId)
    }

    override fun cancelAll() {
        // no-ops
    }

    companion object {

        fun with(context: Context) = GlideImageLoader(context)
    }
}
