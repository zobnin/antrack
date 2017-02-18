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
    Settings settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = Settings.getInstance();
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        context = getActivity().getApplicationContext();
        boolean enabled;
        switch (key) {
            case "enable_service":
                enabled = sharedPreferences.getBoolean(key, true);
                if (enabled) {
                    settings.put(C.S_ENABLE_SERVICE, "true");
                    serviceIntent = new Intent(context, MainService.class);
                    context.startService(serviceIntent);
                    L.d(TAG, "Service started");
                } else {
                    settings.put(C.S_ENABLE_SERVICE, "false");
                    context.stopService(serviceIntent);
                    L.d(TAG, "Service stopped");
                }
                break;
            case "start_at_boot":
                enabled = sharedPreferences.getBoolean(key, true);
                if (enabled) {
                    settings.put(C.S_START_AT_BOOT, "true");
                } else {
                    settings.put(C.S_START_AT_BOOT, "false");
                }
                break;
            case "enable_ctl":
                enabled = sharedPreferences.getBoolean(key, false);
                if (enabled) {
                    settings.put(C.S_ENABLE_CTL, "true");
                    serviceIntent = new Intent(context, MainService.class);
                    serviceIntent.setAction(C.ACTION_CTL_ENABLED);
                } else {
                    settings.put(C.S_ENABLE_CTL, "false");
                    serviceIntent = new Intent(context, MainService.class);
                    serviceIntent.setAction(C.ACTION_CTL_DISABLED);
                }
                context.startService(serviceIntent);
                break;
            case "update_interval":
                String interval = sharedPreferences.getString(key, "30");
                settings.put(C.S_UPDATE_INTERVAL, interval);
                String serviceEnabled = settings.get(C.S_ENABLE_SERVICE);
                if (serviceEnabled == null || serviceEnabled.equals(C.TRUE)) {
                    serviceIntent = new Intent(context, MainService.class);
                    context.stopService(serviceIntent);
                    context.startService(serviceIntent);
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);


    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
}
