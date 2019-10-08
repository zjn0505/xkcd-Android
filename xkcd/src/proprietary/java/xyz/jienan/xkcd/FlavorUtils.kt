package xyz.jienan.xkcd

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*

object FlavorUtils {

    private const val FCM_TOPIC_NEW_COMICS = "new_comics"

    private const val FCM_TOPIC_NEW_WHAT_IF = "new_what_if"

    fun init(app: Application) {
        FirebaseMessaging.getInstance().apply {
            subscribeToTopic(FCM_TOPIC_NEW_COMICS)
            subscribeToTopic(FCM_TOPIC_NEW_WHAT_IF)
        }
    }

    fun updateLocale() {
        if (!BuildConfig.DEBUG) {
            Crashlytics.setString("locale", Locale.getDefault().toString())
        }
    }
}