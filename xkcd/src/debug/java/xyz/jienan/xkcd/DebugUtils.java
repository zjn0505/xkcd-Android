package xyz.jienan.xkcd;

import android.content.Context;

import com.squareup.leakcanary.AndroidExcludedRefs;
import com.squareup.leakcanary.ExcludedRefs;
import com.squareup.leakcanary.LeakCanary;

import timber.log.Timber;

public class DebugUtils {
    public static void init(Context context) {
        if (LeakCanary.isInAnalyzerProcess(context)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        ExcludedRefs excludedRefs = AndroidExcludedRefs.createAppDefaults().build();
        LeakCanary.refWatcher(context).excludedRefs(excludedRefs).buildAndInstall();
    }
}
