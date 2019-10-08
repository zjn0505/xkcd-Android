package xyz.jienan.xkcd.base.firebase

import android.text.TextUtils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import timber.log.Timber
import xyz.jienan.xkcd.BuildConfig
import xyz.jienan.xkcd.model.WhatIfArticle
import xyz.jienan.xkcd.model.XkcdPic
import xyz.jienan.xkcd.model.persist.BoxManager
import xyz.jienan.xkcd.model.persist.SharedPrefManager
import xyz.jienan.xkcd.ui.NotificationUtils

/**
 * Created by jienanzhang on 04/03/2018.
 */

class XkcdFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(refreshedToken: String) {
        super.onNewToken(refreshedToken)
        Timber.d("Refreshed token: %s", refreshedToken)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Timber.d("onMessageReceived: data ${remoteMessage.data}")
        sendNotification(remoteMessage)
        if (BuildConfig.DEBUG) {
            remoteMessage.data.forEach { Timber.d("msg data: , ${it.key}, ${it.value}") }
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param remoteMessage FCM message body received.
     */
    private fun sendNotification(remoteMessage: RemoteMessage?) {
        if (remoteMessage == null) {
            return
        }

        if (!remoteMessage.data["xkcd"].isNullOrBlank()) {
            val xkcdPic = Gson().fromJson(remoteMessage.data["xkcd"], XkcdPic::class.java)
            Timber.d("xkcd noti : $xkcdPic")
            if (xkcdPic?.title != null) {
                xkcdNoti(xkcdPic)
            }
        } else if (!TextUtils.isEmpty(remoteMessage.data["whatif"])) {
            val whatIfArticle = Gson().fromJson(remoteMessage.data["whatif"], WhatIfArticle::class.java)
            Timber.d("what if noti : $whatIfArticle")
            if (whatIfArticle?.title != null) {
                whatIfNoti(whatIfArticle)
            }
        }
    }

    private fun xkcdNoti(xkcdPic: XkcdPic) {
        if (SharedPrefManager.latestXkcd < xkcdPic.num) {
            SharedPrefManager.latestXkcd = xkcdPic.num
            BoxManager.updateAndSave(xkcdPic)
            NotificationUtils.showNotification(this, xkcdPic)
        }
    }

    private fun whatIfNoti(whatIfArticle: WhatIfArticle) {
        if (SharedPrefManager.latestWhatIf < whatIfArticle.num) {
            SharedPrefManager.latestWhatIf = whatIfArticle.num
            BoxManager.updateAndSaveWhatIf(mutableListOf(whatIfArticle))
            NotificationUtils.showNotification(this, whatIfArticle)
        }
    }
}
