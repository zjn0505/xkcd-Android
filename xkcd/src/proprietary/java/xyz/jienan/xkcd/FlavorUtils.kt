package xyz.jienan.xkcd

import android.app.Application
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import timber.log.Timber
import java.util.*

object FlavorUtils {

    private const val FCM_TOPIC_NEW_COMICS = "new_comics"

    private const val FCM_TOPIC_NEW_WHAT_IF = "new_what_if"

    fun init() {
        FirebaseMessaging.getInstance().apply {
            subscribeToTopic(FCM_TOPIC_NEW_COMICS)
            subscribeToTopic(FCM_TOPIC_NEW_WHAT_IF)
        }
    }

    fun updateLocale() {
        if (!BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance()
                    .setCustomKey("locale", Locale.getDefault().toString())
        }
    }

    fun getGmsAvailability(app: Application): Boolean {
        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(app)
        Timber.d("GMS status = $status")
        return status == ConnectionResult.SUCCESS
    }
}