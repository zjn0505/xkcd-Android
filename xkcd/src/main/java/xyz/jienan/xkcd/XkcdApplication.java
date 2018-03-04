package xyz.jienan.xkcd;

import android.app.Application;

import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by Jienan on 2018/3/2.
 */

public class XkcdApplication extends Application {

    private static XkcdApplication mInstance;
    public static XkcdApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        XkcdSideloadUtils.init(this);
        FirebaseMessaging.getInstance().subscribeToTopic("new_comics");
    }
}
