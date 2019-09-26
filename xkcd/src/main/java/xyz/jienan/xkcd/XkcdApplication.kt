package xyz.jienan.xkcd

import android.app.Application
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.crashlytics.android.Crashlytics
import com.google.firebase.messaging.FirebaseMessaging
import xyz.jienan.xkcd.base.glide.GlideImageLoader
import xyz.jienan.xkcd.model.MyObjectBox
import xyz.jienan.xkcd.model.XkcdModel
import xyz.jienan.xkcd.model.persist.BoxManager
import xyz.jienan.xkcd.model.persist.SharedPrefManager
import xyz.jienan.xkcd.model.util.XkcdSideloadUtils
import xyz.jienan.xkcd.ui.xkcdimageview.ImageLoaderFactory
import java.util.*

/**
 * Created by Jienan on 2018/3/2.
 */

class XkcdApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (!DebugUtils.init()) {
            return
        }
        updateLocale()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        AppCompatDelegate.setDefaultNightMode((PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString("pref_dark", "1") ?: "1")
                .toInt())
        instance = this
        val boxStore = MyObjectBox.builder().androidContext(this).maxReaders(300).build()
        DebugUtils.debugDB(this, boxStore)
        BoxManager.init(boxStore)
        XkcdSideloadUtils.init(this)
        SharedPrefManager.init(this)

        ImageLoaderFactory.initialize(GlideImageLoader.with(this))
        FirebaseMessaging.getInstance().apply {
            subscribeToTopic(FCM_TOPIC_NEW_COMICS)
            subscribeToTopic(FCM_TOPIC_NEW_WHAT_IF)
        }
    }

    companion object {

        private const val FCM_TOPIC_NEW_COMICS = "new_comics"

        private const val FCM_TOPIC_NEW_WHAT_IF = "new_what_if"

        var instance: XkcdApplication? = null
            private set
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateLocale()
    }

    private fun updateLocale() {
        XkcdModel.localizedUrl = resources.getString(R.string.api_xkcd_localization)
        Crashlytics.setString("locale", Locale.getDefault().toString())
    }
}
