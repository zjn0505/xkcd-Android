package xyz.jienan.xkcd

import android.app.Application
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import timber.log.Timber
import xyz.jienan.xkcd.base.glide.GlideImageLoader
import xyz.jienan.xkcd.model.MyObjectBox
import xyz.jienan.xkcd.model.XkcdModel
import xyz.jienan.xkcd.model.persist.BoxManager
import xyz.jienan.xkcd.model.persist.SharedPrefManager
import xyz.jienan.xkcd.ui.xkcdimageview.ImageLoaderFactory

/**
 * Created by Jienan on 2018/3/2.
 */

class XkcdApplication : Application(), androidx.work.Configuration.Provider {

    var gmsAvailability = false

    override fun onCreate() {
        super.onCreate()
        if (!DebugUtils.init()) {
            return
        }
        FlavorUtils.init()
        updateLocale()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        AppCompatDelegate.setDefaultNightMode((PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString("pref_dark", "1") ?: "1")
                .toInt())
        instance = this
        val boxStore = MyObjectBox.builder().androidContext(this).maxReaders(300).build()
        DebugUtils.debugDB(this)
        BoxManager.init(boxStore)
        SharedPrefManager.init(this)

        ImageLoaderFactory.initialize(GlideImageLoader.with(this))
        gmsAvailability = FlavorUtils.getGmsAvailability(this)
        Timber.d("GMS availability $gmsAvailability")
    }

    companion object {

        var instance: XkcdApplication? = null
            private set
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateLocale()
    }

    private fun updateLocale() {
        XkcdModel.localizedUrl = resources.getString(R.string.api_xkcd_localization)
        FlavorUtils.updateLocale()
    }

    override fun getWorkManagerConfiguration(): androidx.work.Configuration {
        return androidx.work.Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build()
    }
}
