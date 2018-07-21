package xyz.jienan.xkcd.model.persist;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import xyz.jienan.xkcd.XkcdApplication;
import xyz.jienan.xkcd.model.Quote;

import static xyz.jienan.xkcd.Const.INVALID_ID;
import static xyz.jienan.xkcd.Const.LANDING_TYPE;
import static xyz.jienan.xkcd.Const.LAST_VIEW_WHAT_IF_ID;
import static xyz.jienan.xkcd.Const.LAST_VIEW_XKCD_ID;
import static xyz.jienan.xkcd.Const.PREF_WHAT_IF_SEARCH;
import static xyz.jienan.xkcd.Const.PREF_WHAT_IF_SEARCH_IGNORE_CONTENT;
import static xyz.jienan.xkcd.Const.PREF_ZOOM;
import static xyz.jienan.xkcd.Const.SHARED_PREF_KEY_PRE_QUOTE;
import static xyz.jienan.xkcd.Const.TAG_XKCD;
import static xyz.jienan.xkcd.Const.WHAT_IF_LATEST_INDEX;
import static xyz.jienan.xkcd.Const.XKCD_LATEST_INDEX;

public class SharedPrefManager {

    private final SharedPreferences sharedPreferences;

    private final SharedPreferences.Editor editor;

    private final Gson gson = new Gson();

    public SharedPrefManager() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(XkcdApplication.getInstance());
        editor = sharedPreferences.edit();
    }

    public String getLandingType() {
        return sharedPreferences.getString(LANDING_TYPE, TAG_XKCD);
    }

    public void setLandingType(String landingType) {
        editor.putString(LANDING_TYPE, landingType).apply();
    }

    public long getLatestXkcd() {
        return sharedPreferences.getInt(XKCD_LATEST_INDEX, INVALID_ID);
    }

    public void setLatestXkcd(long latestIndex) {
        editor.putInt(XKCD_LATEST_INDEX, (int) latestIndex).apply();
    }

    public void setLastViewedXkcd(int lastViewed) {
        editor.putInt(LAST_VIEW_XKCD_ID, lastViewed).apply();
    }

    public long getLastViewedXkcd(int latestIndex) {
        return sharedPreferences.getInt(LAST_VIEW_XKCD_ID, latestIndex);
    }

    public long getLatestWhatIf() {
        return sharedPreferences.getLong(WHAT_IF_LATEST_INDEX, INVALID_ID);
    }

    public void setLatestWhatIf(long latestIndex) {
        editor.putLong(WHAT_IF_LATEST_INDEX, latestIndex).apply();
    }

    public void setLastViewedWhatIf(long lastViewed) {
        editor.putLong(LAST_VIEW_WHAT_IF_ID, lastViewed).apply();
    }

    public long getLastViewedWhatIf(long latestIndex) {
        return sharedPreferences.getLong(LAST_VIEW_WHAT_IF_ID, latestIndex);
    }

    public Quote getPreviousQuote() {
        String json = sharedPreferences.getString(SHARED_PREF_KEY_PRE_QUOTE, null);
        return json != null ? gson.fromJson(json, Quote.class) : new Quote();
    }

    public void saveNewQuote(Quote quote) {
        editor.putString(SHARED_PREF_KEY_PRE_QUOTE, gson.toJson(quote)).apply();
    }

    public int getWhatIfZoom() {
        String zoom = sharedPreferences.getString(PREF_ZOOM, "zoom_100");
        return Integer.valueOf(zoom.substring(5));
    }

    public String getWhatIfSearchPref() {
        return sharedPreferences.getString(PREF_WHAT_IF_SEARCH, PREF_WHAT_IF_SEARCH_IGNORE_CONTENT);
    }
}
