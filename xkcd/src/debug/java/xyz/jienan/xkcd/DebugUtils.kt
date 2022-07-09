package xyz.jienan.xkcd

import android.app.Application
import android.content.Context
import com.gu.toolargetool.TooLargeTool.startLogging
import timber.log.Timber

object DebugUtils {
    fun init(): Boolean {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        return true
    }

    fun debugDB(context: Context?) {
        if (BuildConfig.DEBUG) {
            startLogging((context as Application?)!!)
        }
    }
}