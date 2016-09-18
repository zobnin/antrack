package org.antrack.app.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.antrack.app.libs.Files;
import org.antrack.app.libs.Utils;
import org.antrack.app.ui.U;
import org.antrack.app.ui.V;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import app.R;

public class LogsFragment extends BaseFragment {
    final String TAG = "LogsFragment";

    Context context;

    String logsFile;

    TextView textView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Otherwise GetActivity() return null after orientation change
        setRetainInstance(true);

        context = getActivity().getApplicationContext();

        View view = inflater.inflate(R.layout.fragment_textview, container, false);

        textView = (TextView) view.findViewById(R.id.fragment_textview_text);

        // FIXME Now it is hardcoded, but in production must read module info
        logsFile = "/logs";

        onFileUpdate();

        if (!V.currentDevice.isMain()) {
            U.getFileAsync(logsFile);
        }

        textView.setAlpha(0);
        textView.animate().alpha(1);

        return view;
    }

    @Override
    public void onFileUpdate() {
        String path = U.getFullPath(logsFile);

        if (!new File(path).exists()) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<String> logsList = Files.textFileToArray(U.getFullPath(logsFile));

                    if (logsList.isEmpty()) {
                        showNodata();
                        return;
                    }

                    final String logsText = Utils.arrayToStringReverse(logsList.toArray(new String[0]), "\n");

                    if (getActivity() == null) return;

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(logsText);
                            textView.setMovementMethod(new ScrollingMovementMethod());
                        }
                    });
                } catch (IOException e) {
                    Log.e(TAG, "Cat read logs file: " + e.toString());
                }
            }
        }).start();
    }

    @Override
    public String getName() { return "Logs"; }

    @Override
    public String getWatchFile() {
        return logsFile;
    }
}
