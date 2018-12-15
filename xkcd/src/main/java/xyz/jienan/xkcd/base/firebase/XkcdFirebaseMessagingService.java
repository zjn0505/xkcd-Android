package xyz.jienan.xkcd.base.firebase;

import android.text.TextUtils;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import timber.log.Timber;
import xyz.jienan.xkcd.BuildConfig;
import xyz.jienan.xkcd.model.WhatIfArticle;
import xyz.jienan.xkcd.model.XkcdPic;
import xyz.jienan.xkcd.model.persist.BoxManager;
import xyz.jienan.xkcd.model.persist.SharedPrefManager;
import xyz.jienan.xkcd.ui.NotificationUtils;

/**
 * Created by jienanzhang on 04/03/2018.
 */

public class XkcdFirebaseMessagingService extends FirebaseMessagingService {

    private SharedPrefManager sharedPrefManager = new SharedPrefManager();

    private BoxManager boxManager = BoxManager.getInstance();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Timber.d("onMessageReceived: data %s", remoteMessage.getData());
        if (BuildConfig.DEBUG && remoteMessage.getNotification() != null) {
            String body = remoteMessage.getNotification().getBody();
            Timber.d("onMessageReceived: body %s ", body);
            if (remoteMessage.getData().isEmpty()) {
                if ("xkcd".equals(remoteMessage.getNotification().getTitle())) {
                    sendNotification(new RemoteMessage.Builder("xkcd").addData("xkcd", body).build());
                } else if ("whatif".equals(remoteMessage.getNotification().getTitle())) {
                    sendNotification(new RemoteMessage.Builder("whatif").addData("whatif", body).build());
                }
                return;
            }
        }
        sendNotification(remoteMessage);
        if (BuildConfig.DEBUG) {
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
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param remoteMessage FCM message body received.
     */
    private void sendNotification(RemoteMessage remoteMessage) {
        if (!TextUtils.isEmpty(remoteMessage.getData().get("xkcd"))) {
            XkcdPic xkcdPic = new Gson().fromJson(remoteMessage.getData().get("xkcd"), XkcdPic.class);
            Timber.d("xkcd noti : %s", xkcdPic);
            if (xkcdPic != null && !TextUtils.isEmpty(xkcdPic.getTitle())) {
                xkcdNoti(xkcdPic);
            }
        } else if (!TextUtils.isEmpty(remoteMessage.getData().get("whatif"))) {
            WhatIfArticle whatIfArticle = new Gson().fromJson(remoteMessage.getData().get("whatif"), WhatIfArticle.class);
            Timber.d("what if noti : %s", whatIfArticle);
            if (whatIfArticle != null && !TextUtils.isEmpty(whatIfArticle.title)) {
                whatIfNoti(whatIfArticle);
            }
        }
    }

    private void xkcdNoti(XkcdPic xkcdPic) {
        long latestIndex = sharedPrefManager.getLatestXkcd();
        if (latestIndex >= xkcdPic.num) {
            return; // User already read the latest comic
        } else {
            sharedPrefManager.setLatestXkcd(xkcdPic.num);
            boxManager.updateAndSave(xkcdPic);
        }
        NotificationUtils.showNotification(this, xkcdPic);
    }

    private void whatIfNoti(WhatIfArticle whatIfArticle) {
        long latestIndex = sharedPrefManager.getLatestWhatIf();
        if (latestIndex >= whatIfArticle.num) {
            return; // User already read the what if
        } else {
            sharedPrefManager.setLatestWhatIf(whatIfArticle.num);
            boxManager.updateAndSaveWhatIf(Collections.singletonList(whatIfArticle));
        }
        NotificationUtils.showNotification(this, whatIfArticle);
    }
}
