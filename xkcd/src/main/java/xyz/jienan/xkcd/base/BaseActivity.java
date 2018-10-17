package xyz.jienan.xkcd.base;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Map;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
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

    protected void logUXEvent(String event, final Map<String, String> params) {
        Bundle bundle = new Bundle();
        bundle.putString(FIRE_UX_ACTION, event);
        if (params != null && params.size() > 0) {
            for (String key : params.keySet()) {
                String value = params.get(key);
                if (value.matches("-?\\d+")) {
                    bundle.putInt(key, Integer.valueOf(value));
                } else {
                    bundle.putString(key, params.get(key));
                }
            }
        }
        mFirebaseAnalytics.logEvent(FIRE_UX_ACTION, bundle);
    }
}
