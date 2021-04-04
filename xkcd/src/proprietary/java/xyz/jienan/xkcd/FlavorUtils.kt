package xyz.jienan.xkcd

import android.app.Application
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import timber.log.Timber
import java.util.*
import kotlin.concurrent.thread

object FlavorUtils {

    private const val FCM_TOPIC_NEW_COMICS = "new_comics"

    private const val FCM_TOPIC_NEW_WHAT_IF = "new_what_if"

    fun init() {
        FirebaseMessaging.getInstance().apply {
            subscribeToTopic(FCM_TOPIC_NEW_COMICS)
            subscribeToTopic(FCM_TOPIC_NEW_WHAT_IF)
        }
        if (BuildConfig.DEBUG) {
            thread(start = true) {
                Timber.d("FCM id ${FirebaseInstallations.getInstance().id}")
            }

            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Timber.w(task.exception, "Fetching FCM registration token failed")
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                Timber.d("FCM token $token")
            })
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