package xyz.jienan.xkcd;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static xyz.jienan.xkcd.Const.XKCD_LATEST_INDEX;

public class SharedPrefManager {

    private final SharedPreferences sharedPreferences;

    private final SharedPreferences.Editor editor;

    public SharedPrefManager() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(XkcdApplication.getInstance());
        editor = sharedPreferences.edit();
    }

    public void setLatest(long latestIndex) {
        editor.putInt(XKCD_LATEST_INDEX, (int) latestIndex);
        editor.apply();
    }
}
