package xyz.jienan.xkcd;

import android.app.Application;

import com.github.piasy.biv.BigImageViewer;
import com.google.firebase.messaging.FirebaseMessaging;

import io.objectbox.BoxStore;
import xyz.jienan.xkcd.base.glide.GlideImageLoader;
import xyz.jienan.xkcd.model.MyObjectBox;
import xyz.jienan.xkcd.model.util.XkcdSideloadUtils;

/**
 * Created by Jienan on 2018/3/2.
 */

public class XkcdApplication extends Application {

    private static final String FCM_TOPIC_NEW_COMICS = "new_comics";
    private static final String FCM_TOPIC_NEW_WHAT_IF = "new_what_if";

    private static XkcdApplication mInstance;

    private BoxStore boxStore;

    public static XkcdApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (!DebugUtils.init(this)) {
            return;
        }
        mInstance = this;
        boxStore = MyObjectBox.builder().androidContext(this).maxReaders(300).build();
        XkcdSideloadUtils.init(this);
        FirebaseMessaging.getInstance().subscribeToTopic(FCM_TOPIC_NEW_COMICS);
        FirebaseMessaging.getInstance().subscribeToTopic(FCM_TOPIC_NEW_WHAT_IF);
        BigImageViewer.initialize(GlideImageLoader.with(this));
    }

    public BoxStore getBoxStore() {
        return boxStore;
    }
}
