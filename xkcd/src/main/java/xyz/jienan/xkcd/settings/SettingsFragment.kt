package xyz.jienan.xkcd.settings

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import androidx.work.*
import timber.log.Timber
import xyz.jienan.xkcd.Const.*
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.model.WhatIfModel
import xyz.jienan.xkcd.model.XkcdModel
import xyz.jienan.xkcd.model.work.XkcdDownloadWorker
import xyz.jienan.xkcd.model.work.XkcdFastLoadWorker

/**
 * Created by Jienan on 2018/3/9.
 */

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

    private val arrowPref by lazy { findPreference<ListPreference>(PREF_ARROW) }

    private val darkPref by lazy { findPreference<ListPreference>(PREF_DARK_THEME) }

    private val zoomPref by lazy { findPreference<ListPreference>(PREF_ZOOM) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.prefs, rootKey)

        findPreference<SwitchPreferenceCompat>(PREF_FONT)?.onPreferenceChangeListener = this

        arrowPref?.summary = resources.getQuantityString(R.plurals.pref_arrow_summary,
                Integer.valueOf(arrowPref?.entry.toString()), arrowPref?.entry.toString())
        arrowPref?.onPreferenceChangeListener = this
        zoomPref?.onPreferenceChangeListener = this
        darkPref?.onPreferenceChangeListener = this

        if (XkcdModel.localizedUrl.isBlank()) {
            findPreference<PreferenceCategory>("pref_key_xkcd")?.removePreference(findPreference(PREF_XKCD_TRANSLATION))
        }

        findPreference<PreferenceCategory>("pref_key_xkcd")?.findPreference<Preference>("pref_xkcd_preload")?.setOnPreferenceClickListener {

            val xkcdFastLoadRequest: OneTimeWorkRequest =
                    OneTimeWorkRequestBuilder<XkcdFastLoadWorker>()
                            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                            .build()

            val xkcdPreloadRequest: OneTimeWorkRequest =
                    OneTimeWorkRequestBuilder<XkcdDownloadWorker>()
                            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).setRequiresStorageNotLow(true).build())
                            .build()

            val work = WorkManager.getInstance(requireContext()).getWorkInfosByTag("xkcd").get().firstOrNull()

            Timber.d("State = ${work?.state}")

            if (work?.state == WorkInfo.State.SUCCEEDED) {
                WorkManager.getInstance(requireContext()).enqueueUniqueWork("xkcd download", ExistingWorkPolicy.KEEP, xkcdPreloadRequest)
            } else {
                WorkManager.getInstance(requireContext()).beginUniqueWork("xkcd download", ExistingWorkPolicy.KEEP, xkcdFastLoadRequest)
                        .then(xkcdPreloadRequest).enqueue()
            }

            true
        }
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
            PREF_ZOOM -> {
                (preference as ListPreference).value = newValue.toString()
                WhatIfModel.setZoom(Integer.valueOf(newValue.toString().substring(5)))
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
