package org.antrack.app.ui.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import org.antrack.app.C;
import org.antrack.app.ui.U;
import org.antrack.app.ui.V;

import java.io.FileInputStream;
import java.util.Properties;

import app.R;

public class ControlFragment extends BaseFragment {
    final String TAG = "ControlFragment";

    Properties prop;

    String settingsFile = C.SETTINGS_FILE;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Otherwise GetActivity() return null after orientation change
        setRetainInstance(true);

        if (!V.currentDevice.isMain())
            U.getFile(settingsFile);

        // в конце вызывать onfileupdate, он просто будет расставлять значения кнопочек

        View view = inflater.inflate(R.layout.fragment_control, null);

        final Switch hideSwitch = (Switch) view.findViewById(R.id.fragment_control_hide);
        hideSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showHideIconWarning(hideSwitch);
                } else {
                    U.runCommandAsync("hide off");
                }
            }
        });

        Switch lostSwitch = (Switch) view.findViewById(R.id.fragment_control_lost);
        lostSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    U.runCommandAsync("lost on");
                } else {
                    U.runCommandAsync("lost off");
                }
            }
        });

        Button smsButton = (Button) view.findViewById(R.id.fragment_control_sms);
        smsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SendSmsDialog.show(getActivity(), null, null);
            }
        });

        Button callButton = (Button) view.findViewById(R.id.fragment_control_call);
        callButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MakeCallDialog.show(getActivity(), null);
            }
        });

        return view;
    }

    @Override
    public String getName() { return "Control"; }

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

    protected void showHideIconWarning(final Switch switchHideIcon) {
        String title = getResources().getString(R.string.main_hide_icon_warning_title);
        Spanned text = Html.fromHtml(getResources().getString(R.string.main_hide_icon_warning_text));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(text);

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                U.runCommandAsync("hide on");
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

}
