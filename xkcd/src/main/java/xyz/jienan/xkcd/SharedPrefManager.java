package xyz.jienan.xkcd;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static xyz.jienan.xkcd.Const.XKCD_LATEST_INDEX;

public class SharedPrefManager {

    private static final int INVALID_ID = 0;

    private static final String LAST_VIEW_XKCD_ID = "xkcd_last_viewed_id";

    private final SharedPreferences sharedPreferences;

    private final SharedPreferences.Editor editor;

    public SharedPrefManager() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(XkcdApplication.getInstance());
        editor = sharedPreferences.edit();
    }

    public void setLatest(long latestIndex) {
        editor.putInt(XKCD_LATEST_INDEX, (int) latestIndex).apply();
    }

    public long getLatest() {
        return sharedPreferences.getInt(XKCD_LATEST_INDEX, INVALID_ID);
    }

    public void setLastViewed(int lastViewed) {
        editor.putInt(LAST_VIEW_XKCD_ID, lastViewed).apply();
    }

    public long getLastViewed(int latestIndex) {
        return sharedPreferences.getInt(LAST_VIEW_XKCD_ID, latestIndex);
    }
}
