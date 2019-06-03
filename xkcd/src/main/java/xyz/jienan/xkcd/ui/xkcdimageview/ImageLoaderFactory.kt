package xyz.jienan.xkcd.ui.xkcdimageview


/**
 * Created by Piasy{github.com/Piasy} on 06/11/2016.
 * Modified by Charlie Zhang for xkcd project
 *
 * This is not a singleton, you can initialize it multiple times, but before you initialize it
 * again, it will use the same {@link ImageLoader} globally.
 */

object ImageLoaderFactory {
//    companion object {
//        @Volatile
//        private var sInstance: BigImageViewer? = null
//
//        fun initialize(imageLoader: ImageLoader) {
//            sInstance = BigImageViewer(imageLoader)
//        }
//
//        fun imageLoader(): ImageLoader {
//            if (sInstance == null) {
//                throw IllegalStateException("You must initialize BigImageViewer before use it!")
//            }
//            return sInstance!!.mImageLoader
//        }
//
//        fun prefetch(vararg uris: Uri) {
//            if (uris == null) {
//                return
//            }
//
//            val imageLoader = imageLoader()
//            for (uri in uris) {
//                imageLoader.prefetch(uri)
//            }
//        }
//    }

    lateinit var imageLoader: ImageLoader

    fun initialize(imageLoader: ImageLoader) : ImageLoader {
        this.imageLoader = imageLoader
        return imageLoader
    }



}