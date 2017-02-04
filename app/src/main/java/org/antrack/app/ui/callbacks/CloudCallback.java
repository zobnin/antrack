package org.antrack.app.ui.callbacks;

import org.antrack.app.C;
import org.antrack.app.CloudWatcher;
import org.antrack.app.Init;
import org.antrack.app.Pw;
import org.antrack.app.libs.L;
import org.antrack.app.ui.State;

// Callback for update files from cloud
public class CloudCallback implements CloudWatcher.Callback {
    public void onFileUpdate(final String path) {
        if (path.endsWith(C.RESULT_FILE)) {
            Pw pw = Pw.getInstance();
            try {
                pw.getFile(Init.getInstance().DEVICES_DIR + path, path);
            } catch (Exception e) {
                    L.d("CloudCallback", "Error downloading result: " + e.toString());
            }
        }
        else if (State.fragment != null) {
            String watchFile = State.fragment.getWatchFile();
            if (watchFile != null) {
                if (path.contains(watchFile)) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Pw pw = Pw.getInstance();
                                if (pw.isConnected())
                                    pw.getFile(Init.getInstance().DEVICES_DIR + path, path);
                            } catch (Exception e) {
                                L.d("CloudCallback", "Error downloading file: " + e.toString());
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
