package xyz.jienan.xkcd.base;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;

import xyz.jienan.xkcd.R;

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
            setTheme(R.style.NormalAppTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
    }

    protected void logUXEvent(String event) {
        Bundle bundle = new Bundle();
        bundle.putString(FIRE_UX_ACTION, event);
        mFirebaseAnalytics.logEvent(FIRE_UX_ACTION, bundle);
    }
}
