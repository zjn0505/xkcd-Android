package xyz.jienan.xkcd.ui

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.os.Build
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import io.reactivex.Maybe
import io.reactivex.MaybeSource
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xyz.jienan.xkcd.Const.*
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.home.MainActivity
import xyz.jienan.xkcd.model.WhatIfArticle
import xyz.jienan.xkcd.model.XkcdPic
import java.util.concurrent.ExecutionException

object NotificationUtils {

    private const val XKCD_LOGO = "https://xkcd.com/s/0b7742.png"

    private const val WHAT_IF_LOGO = "https://what-if.xkcd.com/imgs/whatif-logo.png"

    private val SOUND_URI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    @SuppressLint("CheckResult")
    fun showNotification(context: Context, xkcdPic: XkcdPic) {
        var width = xkcdPic.width
        var height = xkcdPic.height
        val num = xkcdPic.num.toInt()
        width = if (width == 0) 400 else width
        height = if (height == 0) 400 else height
        val imgUrl = if (xkcdPic.img.contains("xkcd")) xkcdPic.targetImg else xkcdPic.img

        with(context) {
            val title = resources.getStringArray(R.array.notification_titles).random().format(TAG_XKCD)
            val content = getString(R.string.notification_content, num, xkcdPic.title)
            val builder = createNotificationBuilder(title, content, TAG_XKCD)
                    .setContentIntent(getPendingIntent(num, TAG_XKCD))

            getNotificationImgMaybe(width, height, imgUrl, TAG_XKCD)
                    .map { bitmap -> builder.appendLargeBitmap(bitmap).build() }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ notification -> showNotification(notification, TAG_XKCD) }, { Timber.e(it) })
        }
    }

    @SuppressLint("CheckResult")
    fun showNotification(context: Context, article: WhatIfArticle) {
        val width = 400
        val height = 400
        val num = article.num.toInt()
        var imgUrl = article.featureImg

        if (TextUtils.isEmpty(imgUrl)) {
            imgUrl = WHAT_IF_LOGO
        }

        with(context) {
            val title = resources.getStringArray(R.array.notification_titles).random().format(TAG_WHAT_IF)
            val content = getString(R.string.notification_content, num, article.title)
            val builder = createNotificationBuilder(title, content, TAG_WHAT_IF)
                    .setContentIntent(getPendingIntent(num, TAG_WHAT_IF))

            getNotificationImgMaybe(width, height, imgUrl!!, TAG_WHAT_IF)
                    .map { bitmap -> builder.appendLargeBitmap(bitmap).build() }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ notification -> showNotification(notification, TAG_WHAT_IF) }, { Timber.e(it) })
        }
    }

    private fun Context.showNotification(notification: Notification, tag: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(tag, tag,
                    NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(if (tag == TAG_XKCD) 0 else 1, notification)
    }

    private fun Context.getPendingIntent(num: Int, tag: String): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(INDEX_ON_NOTI_INTENT, num)
        intent.putExtra(LANDING_TYPE, tag)
        return PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun Context.createNotificationBuilder(title: String, content: String, tag: String) =
            NotificationCompat.Builder(this, tag)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(true)
                    .setSound(SOUND_URI)

    private fun NotificationCompat.Builder.appendLargeBitmap(bitmap: Bitmap): NotificationCompat.Builder {
        if (bitmap.width != 1 && bitmap.height != 1) {
            setLargeIcon(bitmap)
                    .setStyle(NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap)
                            .bigLargeIcon(null))
        }
        return this
    }

    private fun Context.getNotificationImgMaybe(width: Int, height: Int, url: String, tag: String) =
            Single.just(url)
                    .subscribeOn(Schedulers.io())
                    .map { Glide.with(this).load(url).asBitmap().into(width, height).get() }
                    .onErrorResumeNext(getLogoBitmapSingle(this, tag, width, height))
                    .toMaybe()
                    .onErrorResumeNext(Maybe.empty<Bitmap>())
                    .defaultIfEmpty(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))

    private fun getLogoBitmapSingle(context: Context, tag: String, width: Int, height: Int) =
            Single.just(tag)
                    .map { if (tag == TAG_XKCD) XKCD_LOGO else WHAT_IF_LOGO }
                    .map { logo -> Glide.with(context).load(logo).asBitmap().into(width, height).get() }
}
