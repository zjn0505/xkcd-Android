package xyz.jienan.xkcd.settings

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationManagerCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import timber.log.Timber
import xyz.jienan.xkcd.Const.PREF_ARROW
import xyz.jienan.xkcd.Const.PREF_DARK_THEME
import xyz.jienan.xkcd.Const.PREF_FONT
import xyz.jienan.xkcd.Const.PREF_NOTIFICATION
import xyz.jienan.xkcd.Const.PREF_XKCD_STORAGE
import xyz.jienan.xkcd.Const.PREF_XKCD_TRANSLATION
import xyz.jienan.xkcd.Const.PREF_ZOOM
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

    private val notificationPref by lazy { findPreference<SwitchPreferenceCompat>(PREF_NOTIFICATION) }

    private val notificationManager by lazy { NotificationManagerCompat.from(requireContext()) }

    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    // android.Manifest.permission.POST_NOTIFICATIONS
    private val notificationPermission = "android.permission.POST_NOTIFICATIONS"

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

        notificationPref?.onPreferenceChangeListener = this

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
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            notificationPref?.isChecked = isGranted
            Timber.d("isGranted $isGranted")
            if (!crashFreeShouldShowRequestPermissionRationale(notificationPermission) && !isGranted) {
                notificationPref?.setSummaryOff(R.string.pref_notification_summary_enable_from_settings)
            }
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
            PREF_NOTIFICATION -> {
                Timber.d("$PREF_NOTIFICATION, set to $newValue")

                val notificationEnabled = notificationManager.areNotificationsEnabled()

                if (notificationEnabled) {
                    (preference as SwitchPreferenceCompat).isChecked = (newValue == true)
                } else {
                    if (newValue == true) {
                        if (crashFreeShouldShowRequestPermissionRationale(notificationPermission)) {
                            permissionLauncher.launch(notificationPermission)
                        } else {
                            showNotificationSettings(requireContext())
                        }
                    } else {
                        (preference as SwitchPreferenceCompat).isChecked = false
                    }
                }
            }
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        if (notificationManager.areNotificationsEnabled() && notificationPref?.isChecked == false) {
            notificationPref?.setSummaryOff(R.string.pref_notification_summary_disabled)
        } else if (!notificationManager.areNotificationsEnabled()) {
            notificationPref?.isChecked = false
            if (crashFreeShouldShowRequestPermissionRationale(notificationPermission)) {
                notificationPref?.setSummaryOff(R.string.pref_notification_summary_disabled)
            } else {
                notificationPref?.setSummaryOff(R.string.pref_notification_summary_enable_from_settings)
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun crashFreeShouldShowRequestPermissionRationale(permission: String): Boolean {
        return try {
            shouldShowRequestPermissionRationale(permission)
        } catch (e: Exception) {
            false
        }
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

    private fun showNotificationSettings(context: Context, channelId: String? = null) {
        val notificationSettingsIntent = when {
            // TODO test 26 and above
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O /*26*/ -> Intent().apply {
                action = when (channelId) {
                    null -> Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    else -> Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS
                }
                channelId?.let { putExtra(Settings.EXTRA_CHANNEL_ID, it) }
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P /*28*/) {
                    flags += Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
            // TODO test 21 - 25
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP /*21*/ -> Intent().apply {
                action = "android.settings.APP_NOTIFICATION_SETTINGS"
                putExtra("app_package", context.packageName)
                putExtra("app_uid", context.applicationInfo.uid)
            }
            // TODO test 20 and below
            else -> Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                addCategory(Intent.CATEGORY_DEFAULT)
                data = Uri.parse("package:" + context.packageName)
            }
        }
        startActivity(notificationSettingsIntent)
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
