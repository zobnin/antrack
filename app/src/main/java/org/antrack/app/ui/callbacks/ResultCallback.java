package org.antrack.app.ui.callbacks;

import org.antrack.app.C;
import org.antrack.app.FileWatcher;
import org.antrack.app.libs.L;
import org.antrack.app.ui.MainActivity;
import org.antrack.app.ui.State;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

// Callback watching for result file
public class ResultCallback implements FileWatcher.Callback {
    private static final String TAG = "ResultCallback";

    private MainActivity activity;

    public ResultCallback(MainActivity activity) {
        this.activity = activity;
    }

    public void onFileUpdate(String path) {
        L.d(TAG, "result updated");

        String time = null;
        String result = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            // FIXME check time
            time = reader.readLine();
            result = reader.readLine();
        } catch (IOException e) {
            L.e(TAG, "Can't read result file: " + e.toString());
        }

        State.device.lastUpdate = time.substring(0, time.length()-4);
        State.fragment.onResult(result);
    }

    public String getWatchFile() {
        return "/" + State.device.getDir() + C.RESULT_FILE;
    }
}

