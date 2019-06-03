package xyz.jienan.xkcd.ui.xkcdimageview

import android.net.Uri
import androidx.annotation.UiThread
import java.io.File


interface ImageLoader {

    fun loadImage(requestId: Int, uri: Uri, callback: Callback)

    fun prefetch(uri: Uri)

    fun cancel(requestId: Int)

    fun cancelAll()

    @UiThread
    interface Callback {
        fun onCacheHit(imageType: Int, image: File)

        fun onCacheMiss(imageType: Int, image: File)

        fun onStart()

        fun onProgress(progress: Int)

        fun onFinish()

        fun onSuccess(image: File)

        fun onFail(error: Exception)
    }
}