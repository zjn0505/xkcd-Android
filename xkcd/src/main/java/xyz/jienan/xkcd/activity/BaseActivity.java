package xyz.jienan.xkcd.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import xyz.jienan.xkcd.R;

import static xyz.jienan.xkcd.Const.PREF_FONT;

/**
 * Created by Jienan on 2018/3/9.
 */

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean fontPref = sharedPreferences.getBoolean(PREF_FONT, false);
        if (fontPref) {
            setTheme(R.style.NormalAppTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
    }
}
