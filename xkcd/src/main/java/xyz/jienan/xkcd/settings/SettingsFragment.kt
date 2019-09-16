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

/**
 * Created by Jienan on 2018/3/9.
 */

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

    private val arrowPref by lazy { findPreference<ListPreference>(PREF_ARROW) }

    private val darkPref by lazy { findPreference<ListPreference>(PREF_DARK_THEME) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.prefs, rootKey)

        findPreference<SwitchPreferenceCompat>(PREF_FONT)?.onPreferenceChangeListener = this

        arrowPref?.summary = resources.getQuantityString(R.plurals.pref_arrow_summary,
                Integer.valueOf(arrowPref?.entry.toString()), arrowPref?.entry.toString())
        arrowPref?.onPreferenceChangeListener = this

        darkPref?.onPreferenceChangeListener = this
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        when (preference?.key) {
            PREF_ARROW -> {
                (preference as ListPreference).value = newValue.toString()
                arrowPref?.summary = resources.getQuantityString(R.plurals.pref_arrow_summary,
                        Integer.valueOf(arrowPref?.entry.toString()), arrowPref?.entry.toString())
            }
            PREF_FONT -> {
                (preference as SwitchPreferenceCompat).isChecked = newValue as Boolean
                activity?.recreate()
            }
            PREF_DARK_THEME -> {
                (preference as ListPreference).value = newValue.toString()
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    activity?.setResult(RES_DARK, Intent().also { it.putExtra(PREF_DARK_THEME, newValue.toString().toInt()) })
                    activity?.finish()
                } else {
                    AppCompatDelegate.setDefaultNightMode(newValue.toString().toInt())
                }
            }
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
