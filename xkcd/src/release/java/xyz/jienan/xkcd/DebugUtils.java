package xyz.jienan.xkcd;

import android.content.Context;

import io.objectbox.BoxStore;
import timber.log.Timber;

public class DebugUtils {
    static boolean init() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        return true;
    }

    static void debugDB(Context context, BoxStore boxStore) {
        // no-ops
    }

    static void updateLocale() {
    }
}
