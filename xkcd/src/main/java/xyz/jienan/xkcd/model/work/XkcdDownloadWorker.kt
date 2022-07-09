package xyz.jienan.xkcd.model.work

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.CATEGORY_PROGRESS
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import timber.log.Timber
import xyz.jienan.xkcd.Const.TAG_XKCD
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.model.persist.BoxManager
import xyz.jienan.xkcd.model.persist.SharedPrefManager
import xyz.jienan.xkcd.ui.getPendingIntent
import java.util.*
import java.util.concurrent.ExecutionException

class XkcdDownloadWorker(private val context: Context, parameters: WorkerParameters) :
        CoroutineWorker(context, parameters) {

    private val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as
                    NotificationManager

    private var max: Int = 0

    private var count = 0

    override suspend fun doWork(): Result {
        max = BoxManager.allXkcd.size
        setForeground(createForegroundInfo(0, max))
        val allXkcd = BoxManager.allXkcd

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            allXkcd.parallelStream().forEach {
                if (isStopped) {
                    return@forEach
                }
                Timber.d("Download ${it.num}")
                try {
                    Glide.with(context).load(it.targetImg).downloadOnly(it.width, it.height).get()
                } catch (e: ExecutionException) {
                    Timber.e("Failed to download ${it.num} ${it.targetImg}")
                }
                Timber.i("Progress ${++count}/${allXkcd.size}")
                if (!isStopped) {
                    try {
                        setForegroundAsync(createForegroundInfo(count, max))
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                }
            }
        } else {
            allXkcd.asFlow().collect {
                if (isStopped) {
                    return@collect
                }
                Timber.d("Download ${it.num}")
                withContext(Dispatchers.IO) {
                    Glide.with(context).load(it.targetImg).downloadOnly(it.width, it.height).get()
                }
                Timber.i("Progress ${++count}/${allXkcd.size}")
                if (!isStopped) {
                    setForeground(createForegroundInfo(count, max))
                }
            }
        }
        return Result.success()
    }

    // Creates an instance of ForegroundInfo which can be used to update the
    // ongoing notification.
    private fun createForegroundInfo(progress: Int, max: Int): ForegroundInfo {
        // This PendingIntent can be used to cancel the worker
        return ForegroundInfo(NOTIFICATION_ID, getNotification(progress, max))
    }

    private fun getNotification(progress: Int, max: Int): Notification {
        Timber.d("getNotification progress $progress, max $max")
        return builder.setProgress(max, progress, false)
                .setContentText(context.getString(R.string.xkcd_download_notification_content_text, max - progress))
                .build()
    }

    private val builder: NotificationCompat.Builder by lazy {
        val title = context.getString(R.string.xkcd_download_notification_channel_name)
        val cancel = context.getString(android.R.string.cancel)
        val intent = WorkManager.getInstance(applicationContext)
                .createCancelPendingIntent(id)

        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setContentTitle(title)
                .setTicker(title)
                .setCategory(CATEGORY_PROGRESS)
                .setContentIntent(context.getPendingIntent(SharedPrefManager.latestXkcd.toInt(), TAG_XKCD))
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .addAction(android.R.drawable.ic_delete, cancel, intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.xkcd_download_notification_channel_name).toLowerCase(Locale.getDefault()),
                NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }
}

private const val CHANNEL_ID = "offline_xkcd"

private const val NOTIFICATION_ID = 1