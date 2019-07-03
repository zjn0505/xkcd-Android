package xyz.jienan.xkcd.ui

import android.annotation.SuppressLint
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
import java.util.*
import java.util.concurrent.ExecutionException

object NotificationUtils {

    private const val XKCD_LOGO = "https://xkcd.com/s/0b7742.png"

    private const val WHAT_IF_LOGO = "https://what-if.xkcd.com/imgs/whatif-logo.png"

    fun showNotification(context: Context, xkcdPic: XkcdPic) {
        var width = xkcdPic.width
        var height = xkcdPic.height
        val num = xkcdPic.num.toInt()
        width = if (width == 0) 400 else width
        height = if (height == 0) 400 else height

        val imgUrl = if (xkcdPic.img.contains("xkcd")) xkcdPic.targetImg else xkcdPic.img

        showNotification(context, width, height, num, xkcdPic.title, imgUrl, TAG_XKCD)
    }


    fun showNotification(context: Context, article: WhatIfArticle) {
        val width = 400
        val height = 400
        val num = article.num.toInt()
        var imgUrl = article.featureImg

        if (TextUtils.isEmpty(imgUrl)) {
            imgUrl = WHAT_IF_LOGO
        }
        showNotification(context, width, height, num, article.title, imgUrl!!, TAG_WHAT_IF)
    }

    @SuppressLint("CheckResult")
    private fun showNotification(context: Context, width: Int, height: Int, num: Int, title: String, imgUrl: String, tag: String) {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(INDEX_ON_NOTI_INTENT, num)
        intent.putExtra(LANDING_TYPE, tag)
        val pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        val titles = context.resources.getStringArray(R.array.notification_titles)
        val index = Random().nextInt(titles.size)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        Single.just(imgUrl)
                .subscribeOn(Schedulers.io())
                .map { url -> Glide.with(context).load(url).asBitmap().into(width, height).get() }
                .onErrorResumeNext(Single.just(tag)
                        .map { if (tag == TAG_XKCD) XKCD_LOGO else WHAT_IF_LOGO }
                        .map { logo -> Glide.with(context).load(logo).asBitmap().into(width, height).get() })
                .toMaybe()
                .onErrorResumeNext(MaybeSource {
                    if (it is ExecutionException) {
                        Maybe.empty<Bitmap>()
                    } else {
                        Maybe.error(it as Throwable)
                    }
                })
                .defaultIfEmpty(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
                .map { bitmap ->
                    val builder = NotificationCompat.Builder(context, tag)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setContentTitle(String.format(titles[index], tag))
                            .setContentText(context.getString(R.string.notification_content, num, title))
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent)
                    if (bitmap.width != 1 && bitmap.height != 1) {
                        builder.setLargeIcon(bitmap)
                                .setStyle(NotificationCompat.BigPictureStyle()
                                        .bigPicture(bitmap)
                                        .bigLargeIcon(null))
                    }
                    builder.build()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ notification ->
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    // Since android Oreo notification channel is needed.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channel = NotificationChannel(tag, tag,
                                NotificationManager.IMPORTANCE_DEFAULT)
                        notificationManager.createNotificationChannel(channel)
                    }
                    notificationManager.notify(if (tag == TAG_XKCD) 0 else 1, notification)

                }, { Timber.e(it) })
    }
}
