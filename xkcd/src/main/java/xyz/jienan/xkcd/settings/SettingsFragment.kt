package xyz.jienan.xkcd.settings

import android.app.Activity
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import xyz.jienan.xkcd.Const.*
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.model.WhatIfModel

/**
 * Created by Jienan on 2018/3/9.
 */

class SettingsFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener {

    private val arrowPref by lazy { findPreference(PREF_ARROW) as ListPreference }
    private val randomPref by lazy { findPreference(PREF_RANDOM) as ListPreference }
    private val zoomPref by lazy { findPreference(PREF_ZOOM) as ListPreference }
    private val searchPref by lazy { findPreference(PREF_WHAT_IF_SEARCH) as ListPreference }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.prefs)
        findPreference(PREF_FONT).onPreferenceChangeListener = this
        findPreference(PREF_XKCD_GIF_ECO).onPreferenceChangeListener = this

        arrowPref.summary = resources.getQuantityString(R.plurals.pref_arrow_summary,
                Integer.valueOf(arrowPref.entry.toString()), arrowPref.entry.toString())
        arrowPref.onPreferenceChangeListener = this

        randomPref.summary = randomPref.entry.toString()
        randomPref.onPreferenceChangeListener = this

        searchPref.summary = searchPref.entry.toString()
        searchPref.onPreferenceChangeListener = this

        zoomPref.summary = zoomPref.entry.toString()
        zoomPref.onPreferenceChangeListener = this
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            PREF_ARROW -> {
                (preference as ListPreference).value = newValue.toString()
                arrowPref.summary = resources.getQuantityString(R.plurals.pref_arrow_summary,
                        Integer.valueOf(arrowPref.entry.toString()), arrowPref.entry.toString())
            }
            PREF_RANDOM -> {
                (preference as ListPreference).value = newValue.toString()
                randomPref.summary = randomPref.entry.toString()
            }
            PREF_FONT -> {
                (preference as SwitchPreference).isChecked = newValue as Boolean
                activity.recreate()
                needRecreateForParent = true
            }
            PREF_ZOOM -> {
                (preference as ListPreference).value = newValue.toString()
                zoomPref.summary = zoomPref.entry.toString()
                WhatIfModel.setZoom(Integer.valueOf(newValue.toString().substring(5)))
            }
            PREF_WHAT_IF_SEARCH -> {
                (preference as ListPreference).value = newValue.toString()
                searchPref.summary = searchPref.entry.toString()
            }
            PREF_XKCD_GIF_ECO -> (preference as SwitchPreference).isChecked = newValue as Boolean
        }
        return false
    }

    override fun onStop() {
        if (needRecreateForParent) {
            activity.setResult(Activity.RESULT_OK)
        }
        super.onStop()
    }

    private var needRecreateForParent = false
}
