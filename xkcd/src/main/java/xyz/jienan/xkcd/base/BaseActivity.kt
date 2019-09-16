package xyz.jienan.xkcd.base

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.firebase.analytics.FirebaseAnalytics
import xyz.jienan.xkcd.Const.FIRE_UX_ACTION
import xyz.jienan.xkcd.Const.PREF_FONT
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.comics.activity.ImageDetailPageActivity
import xyz.jienan.xkcd.home.MainActivity

/**
 * Created by Jienan on 2018/3/9.
 */

abstract class BaseActivity : AppCompatActivity() {

    private val mFirebaseAnalytics by lazy { FirebaseAnalytics.getInstance(this) }

    protected val sharedPreferences: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme()
        super.onCreate(savedInstanceState)
    }

    @JvmOverloads
    protected fun logUXEvent(event: String, bundle: Bundle? = null) {
        mFirebaseAnalytics.logEvent(FIRE_UX_ACTION, (bundle ?: Bundle()).apply {
            putString(FIRE_UX_ACTION, event)
        })
    }

    private fun setTheme() {
        val fontPref = sharedPreferences.getBoolean(PREF_FONT, false)
        if (fontPref) {
            when (this) {
                is MainActivity -> setTheme(R.style.CustomActionBarTheme)
                is ImageDetailPageActivity -> setTheme(R.style.TransparentBackgroundTheme)
                else -> setTheme(R.style.AppBarTheme)
            }
        } else {
            when (this) {
                is MainActivity -> setTheme(R.style.CustomActionBarFontTheme)
                is ImageDetailPageActivity -> setTheme(R.style.TransparentBackgroundTheme)
                else -> setTheme(R.style.AppBarFontTheme)
            }
        }
    }
}
