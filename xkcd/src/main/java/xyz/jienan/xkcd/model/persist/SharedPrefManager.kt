package xyz.jienan.xkcd.model.persist

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import android.text.TextUtils
import com.google.gson.Gson
import xyz.jienan.xkcd.Const.*
import xyz.jienan.xkcd.model.Quote

object SharedPrefManager {

    private lateinit var sharedPreferences: SharedPreferences

    private val editor: SharedPreferences.Editor by lazy { sharedPreferences.edit() }

    private val gson = Gson()

    fun init(context: Context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    var landingType: String
        get() = sharedPreferences.getString(LANDING_TYPE, TAG_XKCD)!!
        set(landingType) = editor.putString(LANDING_TYPE, landingType).apply()

    var latestXkcd: Long
        get() = sharedPreferences.getInt(XKCD_LATEST_INDEX, INVALID_ID).toLong()
        set(latestIndex) = editor.putInt(XKCD_LATEST_INDEX, latestIndex.toInt()).apply()

    var latestWhatIf: Long
        get() = sharedPreferences.getLong(WHAT_IF_LATEST_INDEX, INVALID_ID.toLong())
        set(latestIndex) = editor.putLong(WHAT_IF_LATEST_INDEX, latestIndex).apply()

    val previousQuote: Quote
        get() {
            val json = sharedPreferences.getString(SHARED_PREF_KEY_PRE_QUOTE, null)
            return if (!TextUtils.isEmpty(json)) gson.fromJson(json, Quote::class.java) else Quote()
        }

    val whatIfZoom: Int
        get() {
            val zoom = sharedPreferences.getString(PREF_ZOOM, "zoom_100")
            return Integer.valueOf(zoom!!.substring(5))
        }

    val whatIfSearchPref: String
        get() = sharedPreferences.getString(PREF_WHAT_IF_SEARCH, PREF_WHAT_IF_SEARCH_IGNORE_CONTENT)!!

    fun setLastViewedXkcd(lastViewed: Int) {
        editor.putInt(LAST_VIEW_XKCD_ID, lastViewed).apply()
    }

    fun getLastViewedXkcd(latestIndex: Int): Long {
        return sharedPreferences.getInt(LAST_VIEW_XKCD_ID, latestIndex).toLong()
    }

    fun setLastViewedExtra(lastViewed: Int) {
        editor.putInt(LAST_VIEW_EXTRA_ID, lastViewed).apply()
    }

    fun getLastViewedExtra(latestIndex: Int): Int {
        return sharedPreferences.getInt(LAST_VIEW_EXTRA_ID, latestIndex)
    }

    fun setLastViewedWhatIf(lastViewed: Long) {
        editor.putLong(LAST_VIEW_WHAT_IF_ID, lastViewed).apply()
    }

    fun getLastViewedWhatIf(latestIndex: Long): Long {
        return sharedPreferences.getLong(LAST_VIEW_WHAT_IF_ID, latestIndex)
    }

    fun saveNewQuote(quote: Quote) {
        editor.putString(SHARED_PREF_KEY_PRE_QUOTE, gson.toJson(quote)).apply()
    }
}
