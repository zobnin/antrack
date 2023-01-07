@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.antrack.app.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceFragment
import app.R
import org.antrack.app.DEFAULT_UPDATE_INTERVAL
import org.antrack.app.Settings
import org.antrack.app.WIZARD_LAUNCH_CODE
import org.antrack.app.service.CloudService
import org.antrack.app.ui.WizardActivity

class SettingsFragmentNested : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    @Deprecated("Deprecated in Java")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)

        findPreference("run_setup_wizard").setOnPreferenceClickListener {
            WizardActivity.launch(activity, WIZARD_LAUNCH_CODE)
            true
        }
    }

    override fun onSharedPreferenceChanged(sp: SharedPreferences, key: String) {
        when (key) {
            "enable_service" -> toggleEnableService(sp, key)
            "start_at_boot" -> toggleStartAtBoot(sp, key)
            "update_interval" -> toggleUpdateInterval(sp, key)
        }
    }

    private fun toggleUpdateInterval(
        sp: SharedPreferences,
        key: String
    ) {
        val interval = sp.getLong(key, DEFAULT_UPDATE_INTERVAL)
        Settings.updateInterval = interval

        if (Settings.isServiceEnabled) {
            CloudService.stop(activity)
            CloudService.start(activity)
        }
    }

    private fun toggleStartAtBoot(
        sp: SharedPreferences,
        key: String
    ) {
        Settings.startAtBoot = sp.getBoolean(key, true)
    }

    private fun toggleEnableService(
        sp: SharedPreferences,
        key: String
    ) {
        Settings.isServiceEnabled = sp.getBoolean(key, true)
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }
}
