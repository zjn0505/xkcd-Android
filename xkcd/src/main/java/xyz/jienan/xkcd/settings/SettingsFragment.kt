package xyz.jienan.xkcd.settings

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import xyz.jienan.xkcd.Const.*
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.model.WhatIfModel

/**
 * Created by Jienan on 2018/3/9.
 */

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

    private val arrowPref by lazy { findPreference<ListPreference>(PREF_ARROW) }
    private val randomPref by lazy { findPreference<ListPreference>(PREF_RANDOM) }
    private val zoomPref by lazy { findPreference<ListPreference>(PREF_ZOOM) }
    private val searchPref by lazy { findPreference<ListPreference>(PREF_WHAT_IF_SEARCH) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.prefs, rootKey)

        findPreference<SwitchPreferenceCompat>(PREF_FONT)?.onPreferenceChangeListener = this
        findPreference<SwitchPreferenceCompat>(PREF_XKCD_GIF_ECO)?.onPreferenceChangeListener = this

        arrowPref?.summary = resources.getQuantityString(R.plurals.pref_arrow_summary,
                Integer.valueOf(arrowPref?.entry.toString()), arrowPref?.entry.toString())
        arrowPref?.onPreferenceChangeListener = this

        randomPref?.summary = randomPref?.entry.toString()
        randomPref?.onPreferenceChangeListener = this

        searchPref?.summary = searchPref?.entry.toString()
        searchPref?.onPreferenceChangeListener = this

        zoomPref?.summary = zoomPref?.entry.toString()
        zoomPref?.onPreferenceChangeListener = this
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        when (preference?.key) {
            PREF_ARROW -> {
                (preference as ListPreference).value = newValue.toString()
                arrowPref?.summary = resources.getQuantityString(R.plurals.pref_arrow_summary,
                        Integer.valueOf(arrowPref?.entry.toString()), arrowPref?.entry.toString())
            }
            PREF_RANDOM -> {
                (preference as ListPreference).value = newValue.toString()
                randomPref?.summary = randomPref?.entry.toString()
            }
            PREF_FONT -> {
                (preference as SwitchPreferenceCompat).isChecked = newValue as Boolean
                activity?.recreate()
            }
            PREF_ZOOM -> {
                (preference as ListPreference).value = newValue.toString()
                zoomPref?.summary = zoomPref?.entry.toString()
                WhatIfModel.setZoom(Integer.valueOf(newValue.toString().substring(5)))
            }
            PREF_WHAT_IF_SEARCH -> {
                (preference as ListPreference).value = newValue.toString()
                searchPref?.summary = searchPref?.entry.toString()
            }
            PREF_XKCD_GIF_ECO -> (preference as SwitchPreferenceCompat).isChecked = newValue as Boolean
        }
        return false
    }
}
