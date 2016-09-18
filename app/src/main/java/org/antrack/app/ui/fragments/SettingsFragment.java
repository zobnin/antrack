package org.antrack.app.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

import org.antrack.app.C;
import org.antrack.app.service.MainService;
import org.antrack.app.Settings;

import java.util.Arrays;

import app.R;

public class SettingsFragment extends BaseFragment {
    final String TAG = "SettingsFragment";
    Context context;
    Intent serviceIntent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity().getApplicationContext();

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        /*** Enable service switch ***/

        final String serviceEnabled = Settings.get(C.S_ENABLE_SERVICE);

        Switch switchEnableService = (Switch) view.findViewById(R.id.switch_enable_service);

        if (serviceEnabled == null || serviceEnabled.equals("true")) {
            switchEnableService.setChecked(true);
        } else {
            switchEnableService.setChecked(false);
        }

        switchEnableService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Settings.put(C.S_ENABLE_SERVICE, "true");
                    serviceIntent = new Intent(context, MainService.class);
                    context.startService(serviceIntent);
                    Log.d(TAG, "Service started");
                } else {
                    Settings.put(C.S_ENABLE_SERVICE, "false");
                    context.stopService(serviceIntent);
                    Log.d(TAG, "Service stopped");
                }
            }
        });

        /*** Start at boot switch ***/

        Switch switchStartAtBoot = (Switch) view.findViewById(R.id.switch_start_at_boot);

        String startAtBoot = Settings.get(C.S_START_AT_BOOT);
        if (startAtBoot == null || startAtBoot.equals("true")) {
            switchStartAtBoot.setChecked(true);
        } else {
            switchStartAtBoot.setChecked(false);
        }

        switchStartAtBoot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Settings.put(C.S_START_AT_BOOT, "true");
                } else {
                    Settings.put(C.S_START_AT_BOOT, "false");
                }
            }
        });

        /*** Update interval spinner ***/

        int selection = Arrays.asList(C.INTERVALS).indexOf(Settings.get(C.S_UPDATE_INTERVAL));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, C.INTERVALS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final Spinner spinnerUpdateInterval = (Spinner) view.findViewById(R.id.spinner_update_interval);
        spinnerUpdateInterval.setAdapter(adapter);
        // FIXME нужно выбрать именно тот, что указан в настройках
        spinnerUpdateInterval.setSelection(selection);

        spinnerUpdateInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = spinnerUpdateInterval.getSelectedItem().toString();
                Settings.put(C.S_UPDATE_INTERVAL, selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        view.setAlpha(0);
        view.animate().alpha(1);

        return view;
    }

    @Override
    public String getName() { return "Settings"; }

}
