package xyz.jienan.xkcd.settings

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.format.Formatter
import androidx.preference.PreferenceFragmentCompat
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import xyz.jienan.xkcd.BuildConfig
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.model.persist.BoxManager
import xyz.jienan.xkcd.model.persist.SharedPrefManager
import java.io.File


class ManageSpaceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.prefs_storage, rootKey)
        if (BuildConfig.DEBUG) {
            BoxManager.allXkcd.forEachIndexed { index, xkcdPic ->
                val correct = if (index < 403) {
                    xkcdPic.num - 1 == index.toLong()
                } else {
                    xkcdPic.num - 2 == index.toLong()
                }
                if (!correct) {
                    Timber.w("Invalid index $index, pic $xkcdPic")
                    return@forEachIndexed
                }
            }
            Timber.i("Box Store Size ${BoxManager.xkcdBox.store.sizeOnDisk()}")
        }

        findPreference<ButtonPreference>("pref_xkcd_comics")?.apply {
            val xkcdLocalTotal = with(BoxManager.xkcdBox.count()) {
                if (this == 0L) {
                    0
                } else {
                    this + 1 // For missing 404
                }
            }
            val xkcdComicsStatus = getString(R.string.pref_storage_xkcd_comics_summary, SharedPrefManager.latestXkcd, xkcdLocalTotal)
            setup(xkcdComicsStatus, resId = R.string.delete_info) {
                if (xkcdLocalTotal > 0L) {
                    BoxManager.xkcdBox.removeAll()
                    requireActivity().recreate()
                }
            }
        }

        findPreference<ButtonPreference>("pref_xkcd_images")?.apply {
            val glideCacheCount = (Glide.getPhotoCacheDir(requireContext())?.listFiles()?.size
                    ?: 1).minus(1).coerceIn(0, SharedPrefManager.latestXkcd.toInt())
            val totalSize =
                    Glide.getPhotoCacheDir(requireContext())?.walkTopDown()?.filter { it.isFile }?.map { it.length() }?.sum()
                            ?: 0
            val glideCacheSize = Formatter.formatShortFileSize(requireContext(), totalSize)
            val xkcdImagesStatus = getString(R.string.pref_storage_xkcd_images_summary, glideCacheCount, glideCacheSize)
            setup(xkcdImagesStatus, resId = R.string.delete_cache) {
                val glide = Glide.get(requireContext())
                glide.clearMemory()
                CoroutineScope(Dispatchers.IO).launch {
                    glide.clearDiskCache()
                }
                requireActivity().recreate()
            }
        }

        findPreference<ButtonPreference>("pref_xkcd_extra")?.apply {
            val extraLocalTotal = BoxManager.extraBox.count()
            val extraStatus = getString(R.string.pref_storage_xkcd_extra_summary, extraLocalTotal)
            setup(extraStatus, resId = R.string.delete_info) {
                if (extraLocalTotal > 0L) {
                    BoxManager.extraBox.removeAll()
                    requireActivity().recreate()
                }
            }
        }

        findPreference<ButtonPreference>("pref_what_if_articles")?.apply {
            val whatIfLocalTotal = BoxManager.whatIfBox.count()
            val whatIfArticleStatus = getString(R.string.pref_storage_what_if_article_summary, SharedPrefManager.latestWhatIf, whatIfLocalTotal)
            setup(whatIfArticleStatus, resId = R.string.delete_info) {
                if (whatIfLocalTotal > 0L) {
                    BoxManager.whatIfBox.removeAll()
                    requireActivity().recreate()
                }
            }
        }

        findPreference<ButtonPreference>("pref_what_if_cache")?.apply {
            val path = requireContext().cacheDir.path + "/WebView/Default/HTTP Cache"
            Timber.d("WebView cache path $path")
            with(File(path)) {
                val totalSize = if (this.exists() && this.isDirectory) {
                    File(path).walkTopDown().filter { it.isFile }.map { it.length() }.sum()
                } else {
                    0
                }
                val webViewCacheSize = Formatter.formatShortFileSize(requireContext(), totalSize)
                val whatIfCacheStatus = getString(R.string.pref_storage_what_if_cache_summary, webViewCacheSize)
                setup(whatIfCacheStatus, R.string.delete_cache) {
                    if (totalSize > 0) {
                        deleteRecursively()
                        requireActivity().recreate()
                    }
                }
            }
        }

        findPreference<ButtonPreference>("pref_system_clear_data")?.setup(getString(R.string.pref_storage_clear_data_summary), resId = R.string.clear_data) {
            clearAppData()
        }
    }

    private fun clearAppData() {
        try {
            // clearing app data
            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                (requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?)?.clearApplicationUserData() // note: it has a return value!
            } else {
                val runtime = Runtime.getRuntime()
                runtime.exec("pm clear ${BuildConfig.APPLICATION_ID}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}