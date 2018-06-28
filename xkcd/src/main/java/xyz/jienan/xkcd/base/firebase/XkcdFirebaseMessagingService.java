package xyz.jienan.xkcd.base.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import timber.log.Timber;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.home.MainActivity;
import xyz.jienan.xkcd.model.WhatIfArticle;
import xyz.jienan.xkcd.model.XkcdPic;

import static xyz.jienan.xkcd.Const.INDEX_ON_NOTI_INTENT;
import static xyz.jienan.xkcd.Const.LANDING_TYPE;
import static xyz.jienan.xkcd.Const.TAG_WHAT_IF;
import static xyz.jienan.xkcd.Const.TAG_XKCD;
import static xyz.jienan.xkcd.Const.WHAT_IF_LATEST_INDEX;
import static xyz.jienan.xkcd.Const.XKCD_LATEST_INDEX;

/**
 * Created by jienanzhang on 04/03/2018.
 */

public class XkcdFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Timber.d("onMessageReceived: " + remoteMessage.getData());
        sendNotification(remoteMessage);
        Map<String, String> map = remoteMessage.getData();
        if (map != null) {
            Iterator iterator = map.keySet().iterator();

            while (iterator.hasNext()) {
                String key = iterator.next().toString();
                String value = map.get(key);
                Timber.d("msg data: %s  %s", key, value);
            }
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param remoteMessage FCM message body received.
     */
    private void sendNotification(RemoteMessage remoteMessage) {
        XkcdPic xkcdPic = new Gson().fromJson(remoteMessage.getData().get("xkcd"), XkcdPic.class);
        WhatIfArticle whatIfArticle = new Gson().fromJson(remoteMessage.getData().get("whatif"), WhatIfArticle.class);
        if (xkcdPic != null && !TextUtils.isEmpty(xkcdPic.getTitle())) {
            xkcdNoti(xkcdPic);
        } else if (whatIfArticle != null && !TextUtils.isEmpty(whatIfArticle.title)) {
            whatIfNoti(whatIfArticle);
        }
    }

    private void xkcdNoti(XkcdPic xkcdPic) {
        int latestIndex = PreferenceManager.getDefaultSharedPreferences(this).getInt(XKCD_LATEST_INDEX, 0);
        if (latestIndex >= xkcdPic.num) {
            return; // User already read the latest comic
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(INDEX_ON_NOTI_INTENT, (int) xkcdPic.num);
        intent.putExtra(LANDING_TYPE, TAG_XKCD);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        String[] titles = getResources().getStringArray(R.array.notification_titles);
        int index = new Random().nextInt(titles.length);
        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(String.format(titles[index], "xkcd"))
                        .setContentText(getString(R.string.notification_content, xkcdPic.num, xkcdPic.getTitle()))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Xkcd",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private void whatIfNoti(WhatIfArticle whatIfArticle) {
        int latestIndex = PreferenceManager.getDefaultSharedPreferences(this).getInt(WHAT_IF_LATEST_INDEX, 0);
        if (latestIndex >= whatIfArticle.num) {
            return; // User already read the what if
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(INDEX_ON_NOTI_INTENT, (int) whatIfArticle.num);
        intent.putExtra(LANDING_TYPE, TAG_WHAT_IF);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        String[] titles = getResources().getStringArray(R.array.notification_titles);
        int index = new Random().nextInt(titles.length);
        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(String.format(titles[index], "what if"))
                        .setContentText(getString(R.string.notification_content, whatIfArticle.num, whatIfArticle.title))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "What-if",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(1 /* ID of notification */, notificationBuilder.build());
    }
}
