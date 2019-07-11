package xyz.jienan.xkcd.settings

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
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

    private val darkPref by lazy { findPreference<ListPreference>(PREF_DARK_THEME) }

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

        darkPref?.summary = darkPref?.entry.toString()
        darkPref?.onPreferenceChangeListener = this
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
            PREF_DARK_THEME -> {
                (preference as ListPreference).value = newValue.toString()
                darkPref?.summary = darkPref?.entry.toString()
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    activity?.setResult(RES_DARK, Intent().also { it.putExtra(PREF_DARK_THEME, newValue.toString().toInt()) })
                    activity?.finish()
                } else {
                    AppCompatDelegate.setDefaultNightMode(newValue.toString().toInt())
                }
            }
            PREF_XKCD_GIF_ECO -> (preference as SwitchPreferenceCompat).isChecked = newValue as Boolean
        }
        return false
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity?.recreate()
        }
    }

    companion object {
        private const val RES_DARK = 101
    }
}
