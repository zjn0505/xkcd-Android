package xyz.jienan.xkcd.firebase;

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
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.XkcdPic;
import xyz.jienan.xkcd.activity.MainActivity;

import static xyz.jienan.xkcd.Const.XKCD_INDEX_ON_NEW_INTENT;
import static xyz.jienan.xkcd.Const.XKCD_LATEST_INDEX;

/**
 * Created by jienanzhang on 04/03/2018.
 */

public class XkcdFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("Firebase", "onMessageReceived: "+ remoteMessage.getData());
        sendNotification(remoteMessage);
        Map<String, String> map  = remoteMessage.getData();
        if (map != null) {
            Iterator iterator = map.keySet().iterator();

            while (iterator.hasNext()) {
                String key = iterator.next().toString();
                String value = map.get(key);
                Log.d("Firebase", "msg data: " + key + " " + value);
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
        if (xkcdPic == null || TextUtils.isEmpty(xkcdPic.getTitle())) {
            return;
        }
        int latestIndex = PreferenceManager.getDefaultSharedPreferences(this).getInt(XKCD_LATEST_INDEX, 0);
        if (latestIndex >= xkcdPic.num) {
            return; // User already read the latest comic
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(XKCD_INDEX_ON_NEW_INTENT, (int) xkcdPic.num);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteMessage.Notification notification = remoteMessage.getNotification(); // notification.getTitle() .getBody() not used
        String[] titles = getResources().getStringArray(R.array.notification_titles);
        int index = new Random().nextInt(titles.length);
        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(titles[index])
                        .setContentText(getString(R.string.notification_content, xkcdPic.num, xkcdPic.getTitle()))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Xkcd",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
