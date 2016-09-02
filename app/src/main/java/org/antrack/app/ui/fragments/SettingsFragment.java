package org.antrack.app.ui.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
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

        View view = inflater.inflate(R.layout.fragment_settings, null);

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

        /*** Hide icon switch ***/

        final Switch switchHideIcon = (Switch) view.findViewById(R.id.switch_hide_icon);

        String hidden = Settings.get(C.S_HIDDEN);
        if (hidden == null || hidden.equals("false")) {
            switchHideIcon.setChecked(false);
        } else {
            switchHideIcon.setChecked(true);
        }

        switchHideIcon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showHideIconWarning(switchHideIcon);
                } else {
                    serviceIntent = new Intent(context, MainService.class);
                    serviceIntent.putExtra("command", "hide off");
                    context.startService(serviceIntent);
                }
            }
        });

        /*** Remove protection switch ***/

        final Switch switchMakeSystem = (Switch) view.findViewById(R.id.switch_make_system);

        String useRoot = Settings.get(C.S_USE_ROOT);
        if (useRoot == null || useRoot.equals("false")) {
            switchMakeSystem.setEnabled(false);
        }

        String systemApp = Settings.get(C.S_SYSTEM_APP);
        if (systemApp == null || systemApp.equals("false")) {
            switchMakeSystem.setChecked(false);
        } else {
            switchMakeSystem.setChecked(true);
        }

        switchMakeSystem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    serviceIntent = new Intent(context, MainService.class);
                    serviceIntent.putExtra("command", "makesystem");
                    context.startService(serviceIntent);
                } else {
                    // FIXME
                    switchMakeSystem.setChecked(true);
                }
            }
        });

        /*** Update interval spinner ***/

        int selection = Arrays.asList(C.INTERVALS).indexOf(Settings.get(C.S_UPDATE_INTERVAL));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, C.INTERVALS);
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

    protected void showHideIconWarning(final Switch switchHideIcon) {
        String title = getResources().getString(R.string.main_hide_icon_warning_title);
        Spanned text = Html.fromHtml(getResources().getString(R.string.main_hide_icon_warning_text));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(text);

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent myIntent = new Intent(context, MainService.class);
                myIntent.putExtra("command", "hide on");
                context.startService(myIntent);

                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switchHideIcon.setChecked(false);
                dialog.dismiss();
            }
        });

        builder.show();
    }

    @Override
    public String getName() { return "Settings"; }

}
