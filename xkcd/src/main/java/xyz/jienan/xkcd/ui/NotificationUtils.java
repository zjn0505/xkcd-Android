package xyz.jienan.xkcd.ui;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.bumptech.glide.Glide;

import java.util.Random;
import java.util.concurrent.ExecutionException;

import androidx.core.app.NotificationCompat;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.home.MainActivity;
import xyz.jienan.xkcd.model.WhatIfArticle;
import xyz.jienan.xkcd.model.XkcdPic;

import static xyz.jienan.xkcd.Const.INDEX_ON_NOTI_INTENT;
import static xyz.jienan.xkcd.Const.LANDING_TYPE;
import static xyz.jienan.xkcd.Const.TAG_WHAT_IF;
import static xyz.jienan.xkcd.Const.TAG_XKCD;

public class NotificationUtils {

    private static final String XKCD_LOGO = "https://xkcd.com/s/0b7742.png";
    private static final String WHAT_IF_LOGO = "https://what-if.xkcd.com/imgs/whatif-logo.png";

    public static void showNotification(final Context context, XkcdPic xkcdPic) {
        int width = xkcdPic.width, height = xkcdPic.height, num = (int) xkcdPic.num;
        width = width == 0 ? 400 : width;
        height = height == 0 ? 400 : height;

        String imgUrl = xkcdPic.getImg().contains("xkcd") ? xkcdPic.getTargetImg() : xkcdPic.getImg();

        showNotification(context, width, height, num, xkcdPic.getTitle(), imgUrl, TAG_XKCD);
    }


    public static void showNotification(final Context context, WhatIfArticle article) {
        final int width = 400, height = 400, num = (int) article.num;
        String imgUrl = article.featureImg;

        if (TextUtils.isEmpty(imgUrl)) {
            imgUrl = WHAT_IF_LOGO;
        }
        showNotification(context, width, height, num, article.title, imgUrl, TAG_WHAT_IF);
    }

    @SuppressLint("CheckResult")
    private static void showNotification(Context context, int width, int height, int num, String title, String imgUrl, String tag) {
        final Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(INDEX_ON_NOTI_INTENT, num);
        intent.putExtra(LANDING_TYPE, tag);
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        final String[] titles = context.getResources().getStringArray(R.array.notification_titles);
        final int index = new Random().nextInt(titles.length);

        final Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Single.just(imgUrl)
                .subscribeOn(Schedulers.io())
                .map(url -> Glide.with(context).load(url).asBitmap().into(width, height).get())
                .onErrorResumeNext(Single.just(tag)
                        .map(ignored -> tag.equals(TAG_XKCD) ? XKCD_LOGO : WHAT_IF_LOGO)
                        .map(logo -> Glide.with(context).load(logo).asBitmap().into(width, height).get()))
                .toMaybe()
                .onErrorResumeNext(e -> {
                    if (e instanceof ExecutionException) {
                        return Maybe.empty();
                    } else {
                        return Maybe.error(e);
                    }
                })
                .defaultIfEmpty(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
                .map(bitmap -> {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, tag)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setContentTitle(String.format(titles[index], tag))
                            .setContentText(context.getString(R.string.notification_content, num, title))
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);
                    if (bitmap.getWidth() != 1 && bitmap.getHeight() != 1) {
                        builder.setLargeIcon(bitmap)
                                .setStyle(new NotificationCompat.BigPictureStyle()
                                        .bigPicture(bitmap)
                                        .bigLargeIcon(null));
                    }
                    return builder.build();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(notification -> {
                    NotificationManager notificationManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager == null) {
                        return;
                    }
                    // Since android Oreo notification channel is needed.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel(tag,
                                tag,
                                NotificationManager.IMPORTANCE_DEFAULT);
                        notificationManager.createNotificationChannel(channel);
                    }
                    notificationManager.notify(tag.equals(TAG_XKCD) ? 0 : 1, notification);

                }, Timber::e);
    }
}
