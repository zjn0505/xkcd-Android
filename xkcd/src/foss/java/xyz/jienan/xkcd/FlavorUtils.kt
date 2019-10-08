package xyz.jienan.xkcd

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import xyz.jienan.xkcd.base.NotificationWorker
import java.util.concurrent.TimeUnit

object FlavorUtils {
    fun init(app: Application) {
        val notificationWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS).addTag("UpdateXkcd").build()
        WorkManager.getInstance(app)
                .enqueueUniquePeriodicWork("UpdateXkcd", ExistingPeriodicWorkPolicy.KEEP, notificationWorkRequest)
        WorkManager.getInstance(app).cancelAllWork()
    }

    fun updateLocale() {
    }
}