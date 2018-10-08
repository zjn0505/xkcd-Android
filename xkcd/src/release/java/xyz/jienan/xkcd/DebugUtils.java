package xyz.jienan.xkcd;

import android.content.Context;

import timber.log.Timber;

public class DebugUtils {
    public static boolean init(Context context) {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        return true;
    }
}
