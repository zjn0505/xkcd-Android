package xyz.jienan.xkcd.settings

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
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
import xyz.jienan.xkcd.ui.ToastUtils

/**
 * Created by Jienan on 2018/3/9.
 */

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

    private val arrowPref by lazy { findPreference<ListPreference>(PREF_ARROW) }

    private val darkPref by lazy { findPreference<ListPreference>(PREF_DARK_THEME) }

    private val zoomPref by lazy { findPreference<ListPreference>(PREF_ZOOM) }

    private val storagePref by lazy { findPreference<ListPreference>(PREF_XKCD_STORAGE) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        Timber.d("onCreatePreferences")
        setPreferencesFromResource(R.xml.prefs, rootKey)

        findPreference<SwitchPreferenceCompat>(PREF_FONT)?.onPreferenceChangeListener = this

        arrowPref?.summary = resources.getQuantityString(R.plurals.pref_arrow_summary,
                Integer.valueOf(arrowPref?.entry.toString()), arrowPref?.entry.toString())
        arrowPref?.onPreferenceChangeListener = this
        zoomPref?.onPreferenceChangeListener = this
        darkPref?.onPreferenceChangeListener = this
        storagePref?.onPreferenceChangeListener = this


        if (XkcdModel.localizedUrl.isBlank()) {
            findPreference<PreferenceCategory>("pref_key_xkcd")?.removePreference(findPreference(PREF_XKCD_TRANSLATION)!!)
        }

        if (!requireContext().externalMemoryAvailable()) {
            findPreference<PreferenceCategory>("pref_key_xkcd")?.removePreference(findPreference(PREF_XKCD_STORAGE)!!)
        }

        findPreference<PreferenceCategory>("pref_key_xkcd")?.findPreference<Preference>("pref_xkcd_preload")?.setOnPreferenceClickListener {
            offlineWork()
            true
        }

        findPreference<PreferenceCategory>("pref_key_system")?.findPreference<Preference>("pref_system_space")?.setOnPreferenceClickListener {
            startActivity(Intent(context, ManageSpaceActivity::class.java))
            true
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        when (preference.key) {
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
            PREF_XKCD_STORAGE -> {
                ToastUtils.showToast(requireContext(), getString(R.string.pref_xkcd_storage_toast), duration = Toast.LENGTH_LONG)
                return true
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

    private fun offlineWork() {
        Timber.d("offlineWork")
        val xkcdFastLoadRequest: OneTimeWorkRequest =
                OneTimeWorkRequestBuilder<XkcdFastLoadWorker>()
                        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                        .build()

        val xkcdPreloadRequest: OneTimeWorkRequest =
                OneTimeWorkRequestBuilder<XkcdDownloadWorker>()
                        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                                .setRequiresStorageNotLow(true).build())
                        .addTag("xkcd_download")
                        .build()

        val work = WorkManager.getInstance(requireContext()).getWorkInfosByTag("xkcd").get().firstOrNull()

        Timber.d("State = ${work?.state}")

        if (work?.state == WorkInfo.State.SUCCEEDED) {
            WorkManager.getInstance(requireContext()).enqueueUniqueWork("xkcd download", ExistingWorkPolicy.KEEP, xkcdPreloadRequest)
        } else {
            WorkManager.getInstance(requireContext()).beginUniqueWork("xkcd download", ExistingWorkPolicy.KEEP, xkcdFastLoadRequest)
                    .then(xkcdPreloadRequest).enqueue()
        }
    }

    companion object {
        private const val RES_DARK = 101
    }
}

fun Context.externalMemoryAvailable(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getExternalFilesDir(null) != null) {
        try {
            getExternalFilesDirs(null).any {
                try {
                    !Environment.isExternalStorageEmulated(it) && Environment.isExternalStorageRemovable(it)
                } catch (e: Exception) {
                    Timber.w(e, "Failed to check external storage")
                    false
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to check external storage")
            false
        }
    } else {
        false
    }
}
