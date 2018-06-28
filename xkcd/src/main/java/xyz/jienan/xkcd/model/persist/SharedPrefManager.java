package xyz.jienan.xkcd.model.persist;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import xyz.jienan.xkcd.XkcdApplication;

import static xyz.jienan.xkcd.Const.INVALID_ID;
import static xyz.jienan.xkcd.Const.LANDING_TYPE;
import static xyz.jienan.xkcd.Const.LAST_VIEW_WHAT_IF_ID;
import static xyz.jienan.xkcd.Const.LAST_VIEW_XKCD_ID;
import static xyz.jienan.xkcd.Const.TAG_XKCD;
import static xyz.jienan.xkcd.Const.WHAT_IF_LATEST_INDEX;
import static xyz.jienan.xkcd.Const.XKCD_LATEST_INDEX;

public class SharedPrefManager {

    private final SharedPreferences sharedPreferences;

    private final SharedPreferences.Editor editor;

    public SharedPrefManager() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(XkcdApplication.getInstance());
        editor = sharedPreferences.edit();
    }

    public void setLandingType(String landingType) {
        editor.putString(LANDING_TYPE, landingType).apply();
    }

    public String getLandingType() {
        return sharedPreferences.getString(LANDING_TYPE, TAG_XKCD);
    }

    public void setLatestXkcd(long latestIndex) {
        editor.putInt(XKCD_LATEST_INDEX, (int) latestIndex).apply();
    }

    public long getLatestXkcd() {
        return sharedPreferences.getInt(XKCD_LATEST_INDEX, INVALID_ID);
    }

    public void setLastViewedXkcd(int lastViewed) {
        editor.putInt(LAST_VIEW_XKCD_ID, lastViewed).apply();
    }

    public long getLastViewedXkcd(int latestIndex) {
        return sharedPreferences.getInt(LAST_VIEW_XKCD_ID, latestIndex);
    }

    public void setLatestWhatIf(long latestIndex) {
        editor.putLong(WHAT_IF_LATEST_INDEX, latestIndex).apply();
    }

    public long getLatestWhatIf() {
        return sharedPreferences.getLong(WHAT_IF_LATEST_INDEX, INVALID_ID);
    }

    public void setLastViewedWhatIf(long lastViewed) {
        editor.putLong(LAST_VIEW_WHAT_IF_ID, lastViewed).apply();
    }

    public long getLastViewedWhatIf(long latestIndex) {
        return sharedPreferences.getLong(LAST_VIEW_WHAT_IF_ID, latestIndex);
    }
}
