package org.antrack.app.ui.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceFragment
import app.R
import org.antrack.app.C
import org.antrack.app.Settings
import org.antrack.app.libs.L
import org.antrack.app.service.MainService

class SettingsFragmentNested : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    internal val TAG = "SettingsFragment"

    lateinit var serviceIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.preferences)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val enabled: Boolean
        when (key) {
            "enable_service" -> {
                enabled = sharedPreferences.getBoolean(key, true)
                if (enabled) {
                    Settings.put(C.S_ENABLE_SERVICE, "true")
                    serviceIntent = Intent(activity.applicationContext, MainService::class.java)
                    activity.startService(serviceIntent)
                    L.d(TAG, "Service started")
                } else {
                    Settings.put(C.S_ENABLE_SERVICE, "false")
                    activity.stopService(serviceIntent)
                    L.d(TAG, "Service stopped")
                }
            }
            "start_at_boot" -> {
                enabled = sharedPreferences.getBoolean(key, true)
                if (enabled) {
                    Settings.put(C.S_START_AT_BOOT, "true")
                } else {
                    Settings.put(C.S_START_AT_BOOT, "false")
                }
            }
            "enable_ctl" -> {
                enabled = sharedPreferences.getBoolean(key, false)
                if (enabled) {
                    Settings.put(C.S_ENABLE_CTL, "true")
                    serviceIntent = Intent(activity, MainService::class.java)
                    serviceIntent.action = C.ACTION_CTL_ENABLED
                } else {
                    Settings.put(C.S_ENABLE_CTL, "false")
                    serviceIntent = Intent(activity, MainService::class.java)
                    serviceIntent.action = C.ACTION_CTL_DISABLED
                }
                activity.startService(serviceIntent)
            }
            "enable_updater" -> {
                enabled = sharedPreferences.getBoolean(key, true)
                if (enabled) {
                    Settings.put(C.S_ENABLE_UPLOADER, "true")
                } else {
                    Settings.put(C.S_ENABLE_UPLOADER, "false")
                }
            }
            "update_interval" -> {
                val interval = sharedPreferences.getString(key, "30")
                Settings.put(C.S_UPDATE_INTERVAL, interval!!)
                val serviceEnabled = Settings[C.S_ENABLE_SERVICE]
                if (serviceEnabled == null || serviceEnabled == C.TRUE) {
                    serviceIntent = Intent(activity, MainService::class.java)
                    activity.stopService(serviceIntent)
                    activity.startService(serviceIntent)
                }
            }
        }
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
