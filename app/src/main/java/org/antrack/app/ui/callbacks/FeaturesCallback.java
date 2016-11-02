package org.antrack.app.ui.callbacks;

import org.antrack.app.C;
import org.antrack.app.FileWatcher;
import org.antrack.app.libs.LoadingDialog;
import org.antrack.app.ui.MainActivity;
import org.antrack.app.ui.State;

// Callback for update features
public class FeaturesCallback implements FileWatcher.Callback {
    static boolean active = false;

    private MainActivity activity;

    public FeaturesCallback(MainActivity activity) {
        this.activity = activity;
        active = true;
    }

    public void onFileUpdate(String path) {
        FileWatcher fw;
        fw = FileWatcher.getInstance();
        fw.removeCallback("features");
        active = false;

        if (ModulesCallback.active) return;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                activity.switchDevice(true);
                LoadingDialog.hide(activity);
            }
        });
    }

    public String getWatchFile() {
        return "/" + State.device.getDir() + C.FEATURES_FILE;
    }
}

