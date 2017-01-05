package org.antrack.app.ui.callbacks;

import android.util.Log;

import org.antrack.app.C;
import org.antrack.app.FileWatcher;
import org.antrack.app.libs.Files;
import org.antrack.app.libs.L;
import org.antrack.app.libs.Utils;
import org.antrack.app.ui.MainActivity;
import org.antrack.app.ui.State;

import java.io.IOException;

// Callback watching for result file
public class ResultCallback implements FileWatcher.Callback {
    private static final String TAG = "ResultCallback";

    private MainActivity activity;

    public ResultCallback(MainActivity activity) {
        this.activity = activity;
    }

    public void onFileUpdate(String path) {
        L.d(TAG, "RESULT UPDATED!!!");
        String result = "";
        try {
            result = Files.readTextFile(path);
        } catch (IOException e) {
            L.e(TAG, "Can't read result file: " + e);
        }

        final String result2 = result;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showToast(activity.getApplicationContext(), result2);
            }
        });
    }

    public String getWatchFile() {
        return "/" + State.device.getDir() + C.RESULT_FILE;
    }
}

