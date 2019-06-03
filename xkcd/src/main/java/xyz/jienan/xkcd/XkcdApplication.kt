package xyz.jienan.xkcd

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.messaging.FirebaseMessaging
import xyz.jienan.xkcd.base.glide.GlideImageLoader
import xyz.jienan.xkcd.model.MyObjectBox
import xyz.jienan.xkcd.model.persist.BoxManager
import xyz.jienan.xkcd.model.persist.SharedPrefManager
import xyz.jienan.xkcd.model.util.XkcdSideloadUtils
import xyz.jienan.xkcd.ui.xkcdimageview.ImageLoaderFactory

/**
 * Created by Jienan on 2018/3/2.
 */

class XkcdApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (!DebugUtils.init(this)) {
            return
        }
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
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
}
