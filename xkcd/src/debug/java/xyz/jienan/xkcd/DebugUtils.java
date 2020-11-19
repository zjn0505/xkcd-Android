package xyz.jienan.xkcd;

import android.app.Application;
import android.content.Context;

import com.gu.toolargetool.TooLargeTool;

import timber.log.Timber;

public class DebugUtils {
    static boolean init() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        return true;
    }

    static void debugDB(Context context) {
        if (BuildConfig.DEBUG) {
            TooLargeTool.startLogging((Application) context);
        }
    }
}
