package org.antrack.app.ui.callbacks;

import android.util.Log;

import org.antrack.app.CloudWatcher;
import org.antrack.app.Init;
import org.antrack.app.Pw;
import org.antrack.app.ui.State;

// Callback for update files from cloud
public class CloudCallback implements CloudWatcher.Callback {
    public void onFileUpdate(final String path) {
        if (State.fragment != null) {
            String watchFile = State.fragment.getWatchFile();
            if (watchFile != null) {
                if (path.contains(watchFile)) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Pw pw = Pw.getInstance();
                                if (pw.isConnected())
                                    pw.getFile(Init.DEVICES_DIR + path, path);
                            } catch (Exception e) {
                                Log.d("CloudCallback", "Error downloading file: " + e);
                            }
                        }
                    }).start();
                }
            }
        }
    }

    public String getWatchFile() {
        return "/" + State.device.getDir() + "/";
    }
}
