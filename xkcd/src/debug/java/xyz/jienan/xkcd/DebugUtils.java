package xyz.jienan.xkcd;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;

import timber.log.Timber;

public class DebugUtils {
    public static boolean init(Context context) {
        if (LeakCanary.isInAnalyzerProcess(context)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return false;
        }
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        LeakCanary.install((Application) context);
        return true;
    }
}
