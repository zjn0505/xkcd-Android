package xyz.jienan.xkcd

import android.app.Application
import androidx.work.*
import androidx.work.PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS
import xyz.jienan.xkcd.base.NotificationWorker
import java.util.concurrent.TimeUnit

object FlavorUtils {
    fun init(app: Application) {
        val notificationWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(MIN_PERIODIC_INTERVAL_MILLIS,
                TimeUnit.MILLISECONDS)
                .addTag("Update")
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
        WorkManager.getInstance(app)
                .enqueueUniquePeriodicWork("UpdateXkcd",
                        ExistingPeriodicWorkPolicy.KEEP,
                        notificationWorkRequest)
    }

    fun updateLocale() {
    }
}