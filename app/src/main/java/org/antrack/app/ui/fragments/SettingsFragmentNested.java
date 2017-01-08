package org.antrack.app.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.antrack.app.C;
import org.antrack.app.Settings;
import org.antrack.app.libs.L;
import org.antrack.app.service.MainService;

import app.R;

public class SettingsFragmentNested extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    final String TAG = "SettingsFragment";
    Context context;
    Intent serviceIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        boolean enabled;
        switch (key) {
            case "enable_service":
                enabled = sharedPreferences.getBoolean(key, true);
                if (enabled) {
                    Settings.put(C.S_ENABLE_SERVICE, "true");
                    serviceIntent = new Intent(context, MainService.class);
                    context.startService(serviceIntent);
                    L.d(TAG, "Service started");
                } else {
                    Settings.put(C.S_ENABLE_SERVICE, "false");
                    context.stopService(serviceIntent);
                    L.d(TAG, "Service stopped");
                }
                break;
            case "start_at_boot":
                enabled = sharedPreferences.getBoolean(key, true);
                if (enabled) {
                    Settings.put(C.S_START_AT_BOOT, "true");
                } else {
                    Settings.put(C.S_START_AT_BOOT, "false");
                }
                break;
            case "update_interval":
                String interval = sharedPreferences.getString(key, "30");
                Settings.put(C.S_UPDATE_INTERVAL, interval);
                String serviceEnabled = Settings.get(C.S_ENABLE_SERVICE);
                if (serviceEnabled == null || serviceEnabled.equals(C.TRUE)) {
                    serviceIntent = new Intent(context, MainService.class);
                    context.stopService(serviceIntent);
                    context.startService(serviceIntent);
                }
                break;
        }
    }
}
