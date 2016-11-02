package org.antrack.app.ui.callbacks;

import android.util.Log;

import org.antrack.app.FileWatcher;
import org.antrack.app.ui.State;

// Callback for update fragments on file changes
public class FragmentCallback implements FileWatcher.Callback {
    private String watchFile = null;

    public void onFileUpdate(String path) {
        if (State.fragment != null) {
            State.fragment.onFileUpdate();
            Log.d("FragmentCallback", "Fragment updated");
        }
    }

    public String getWatchFile() {
        if (State.fragment != null) {
            watchFile = "/" + State.device.getDir() + State.fragment.getWatchFile();
        }
        return watchFile;
    }
}
