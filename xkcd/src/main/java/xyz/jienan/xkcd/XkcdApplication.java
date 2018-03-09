package xyz.jienan.xkcd;

import android.app.Application;

import com.github.piasy.biv.BigImageViewer;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.leakcanary.LeakCanary;

import xyz.jienan.xkcd.glide.GlideImageLoader;

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
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        mInstance = this;
        XkcdSideloadUtils.init(this);
        FirebaseMessaging.getInstance().subscribeToTopic("new_comics");
        BigImageViewer.initialize(GlideImageLoader.with(getApplicationContext()));
    }
}
