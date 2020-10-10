package xyz.jienan.xkcd.base.glide

import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory.CacheDirectoryGetter
import timber.log.Timber
import java.io.File

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class ExternalSDCacheDiskCacheFactory @JvmOverloads constructor(context: Context, diskCacheName: String? = DEFAULT_DISK_CACHE_DIR, diskCacheSize: Int = DEFAULT_DISK_CACHE_SIZE) :
        DiskLruCacheFactory(CacheDirectoryGetter {

            val file = try {
                context.externalCacheDirs.firstOrNull {
                    try {
                        !Environment.isExternalStorageEmulated(it) && Environment.isExternalStorageRemovable(it)
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to check external storage")
                        false
                    }
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to check external storage")
                null
            }

            val cacheDirectory = file ?: context.cacheDir ?: null

            if (diskCacheName != null) {
                Timber.d("Use cache directory $cacheDirectory")
                File(cacheDirectory, diskCacheName)
            }
            cacheDirectory
        }, diskCacheSize) {
    constructor(context: Context, diskCacheSize: Int) : this(context, DEFAULT_DISK_CACHE_DIR, diskCacheSize)
}