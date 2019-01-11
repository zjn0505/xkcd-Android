package xyz.jienan.xkcd.base;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.firebase.analytics.FirebaseAnalytics;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.comics.activity.ImageDetailPageActivity;
import xyz.jienan.xkcd.home.MainActivity;

import static xyz.jienan.xkcd.Const.FIRE_UX_ACTION;
import static xyz.jienan.xkcd.Const.PREF_FONT;

/**
 * Created by Jienan on 2018/3/9.
 */

public abstract class BaseActivity extends AppCompatActivity {

    protected FirebaseAnalytics mFirebaseAnalytics;

    protected SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        boolean fontPref = sharedPreferences.getBoolean(PREF_FONT, false);
        if (fontPref) {
            if (this instanceof MainActivity) {
                setTheme(R.style.CustomActionBarTheme);
            } else if (this instanceof ImageDetailPageActivity) {
                setTheme(R.style.FullScreenTheme);
            } else {
                setTheme(R.style.AppNoBarTheme);
            }
        } else {
            if (this instanceof MainActivity) {
                setTheme(R.style.CustomActionBarFontTheme);
            } else if (this instanceof ImageDetailPageActivity) {
                setTheme(R.style.FullScreenTheme);
            } else {
                setTheme(R.style.AppNoBarFontTheme);
            }
        }

        super.onCreate(savedInstanceState);
    }

    protected void logUXEvent(String event) {
        logUXEvent(event, null);
    }

    protected void logUXEvent(String event, Bundle bundle) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putString(FIRE_UX_ACTION, event);
        mFirebaseAnalytics.logEvent(FIRE_UX_ACTION, bundle);
    }
}
