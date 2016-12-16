package org.antrack.app.ui.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import org.antrack.app.C;
import org.antrack.app.Init;
import org.antrack.app.ui.State;
import org.antrack.app.ui.U;

import java.io.FileInputStream;
import java.util.Properties;

import app.R;

public class ControlFragment extends BaseFragment {
    final String TAG = "ControlFragment";

    private Switch hideSwitch;
    private Switch systemSwitch;
    private Switch lostSwitch;

    private Button lostButton;
    private Button wipeButton;
    private Button lockButton;
    private Button alarmButton;
    private Button smsButton;
    private Button callButton;

    Properties prop;

    String settingsFile = C.SETTINGS_FILE;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Otherwise GetActivity() return null after orientation change
        setRetainInstance(true);

        if (!State.device.isMain())
            U.getFile(settingsFile);

        // в конце вызывать onfileupdate, он просто будет расставлять значения кнопочек

        View view = inflater.inflate(R.layout.fragment_control, null);

        /*** Hide switch ***/

        hideSwitch = (Switch) view.findViewById(R.id.fragment_control_hide);
        hideSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showHideIconWarning();
                } else {
                    U.runCommandAsync("hide off");
                }
            }
        });

        if (!Mod.check(Mod.HIDE)) {
            hideSwitch.setEnabled(false);
        }

        /*** System switch ***/

        // TODO
        systemSwitch = (Switch) view.findViewById(R.id.fragment_control_system);
        systemSwitch.setEnabled(false);

        /*** Lost switch ***/

        lostSwitch = (Switch) view.findViewById(R.id.fragment_control_lost);
        lostSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // FIXME Mod.LOST not exist
                if (isChecked) {
                    U.runCommandAsync("lost on");
                } else {
                    U.runCommandAsync("lost off");
                }
            }
        });

        /*** Wipe button ***/

        // TODO проверка прав админа

        wipeButton = (Button) view.findViewById(R.id.fragment_control_wipe);
        wipeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showWipeWarning();
            }
        });

        if (!Mod.check(Mod.WIPE)) {
            wipeButton.setEnabled(false);
        }

        /*** Lock button ***/

        // TODO проверка прав админа

        lockButton = (Button) view.findViewById(R.id.fragment_control_lock);
        lockButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showLockWarning();
            }
        });

        if (!Mod.check(Mod.LOCK)) {
            lockButton.setEnabled(false);
        }

        /*** Alarm button ***/

        alarmButton = (Button) view.findViewById(R.id.fragment_control_alarm);
        alarmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // FIXME
                U.runCommandAsync(Mod.getCommand(Mod.ALARM) + " " + Init.APP_DIR + "/" + C.ALARM_ASSET);
            }
        });

        if (!Mod.check(Mod.ALARM)) {
            alarmButton.setEnabled(false);
        }

        /*** SMS / Calls ***/

        smsButton = (Button) view.findViewById(R.id.fragment_control_sms);
        smsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SendSmsDialog.show(getActivity(), null, null);
            }
        });

        callButton = (Button) view.findViewById(R.id.fragment_control_call);
        callButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CallDialog.show(getActivity(), null);
            }
        });

        return view;
    }

    @Override
    public String getWatchFile() {
        return settingsFile;
    }

    @Override
    public void onFileUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    prop = new Properties();
                    prop.load(new FileInputStream(settingsFile));
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                    return;
                }
            }
        }).start();
    }

    protected void showHideIconWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.warning);
        builder.setMessage(R.string.hide_icon_warning);

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // FIXME
                U.runCommandAsync("hide on");
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                hideSwitch.setChecked(false);
                dialog.dismiss();
            }
        });

        builder.show();
    }

    protected void showWipeWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.warning);

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = inflater.inflate(R.layout.dialog_wipe, null, false);

        builder.setView(v);

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkbox);
                if (checkBox.isChecked()) {
                    //U.runCommandAsync(
                    //        Mod.getCommand(Mod.WIPESD) + "; " +
                    //        Mod.getCommand(Mod.WIPE));
                } else {
                    //U.runCommandAsync(Mod.getCommand(Mod.WIPE));
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    protected void showLockWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.warning);

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = inflater.inflate(R.layout.dialog_lock, null, false);

        builder.setView(v);

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                EditText editText = (EditText) v.findViewById(R.id.editText);
                U.runCommandAsync(Mod.getCommand(Mod.LOCK) + " " + editText.getText());
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }
}
