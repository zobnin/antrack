package org.antrack.app;

import android.util.Log;

import org.antrack.app.libs.RecursiveFileObserver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FileWatcher {
    private static final String TAG = "FileWatcher";

    private static volatile FileWatcher instance;
    public static FileWatcher getInstance() {
        FileWatcher localInstance = instance;
        if (localInstance == null) {
            synchronized (FileWatcher.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new FileWatcher();
                }
            }
        }
        return localInstance;
    }

    private HashMap<String, Watcher> watchers;

    private FileWatcher() {
        watchers = new HashMap<>();
    }

    public interface Callback {
        void onFileUpdate(String path);
        // As first dir file must contain device dir
        String getWatchFile();
    }

    private class Watcher extends RecursiveFileObserver {
        private HashMap<String, Callback> callbacks;

        Watcher(String device) {
            super(Init.DEVICES_DIR + device);
            callbacks = new HashMap<>();
        }

        HashMap<String, Callback> getCallbacks() {
            return callbacks;
        }

        void addCallback(String name, Callback callback) {
            callbacks.put(name, callback);
        }

        void removeCallback(String name) {
            callbacks.remove(name);
        }

        @Override
        public void onEvent(int event, String path) {
            if (path == null) {
                return;
            }
            processFile(path);
        }
    }

    public void addCallback(String name, Callback callback) {
        if (callback == null) {
            Log.e(TAG, "addCallback: callback == null");
            return;
        }

        if (callback.getWatchFile() == null)
            return;

        String device = callback.getWatchFile().split("/")[1];

        if (!watchers.containsKey(device)) {
            Watcher watcher = new Watcher(device);
            watcher.addCallback(name, callback);
            watchers.put(device, watcher);
            watcher.startWatching();
        } else {
            // Remove "old" key
            /*
            if (watchers.get(device).getCallbacks().containsKey(name))
                watchers.get(device).getCallbacks().remove(name);
                */
            watchers.get(device).addCallback(name, callback);
        }

        Log.d(TAG, "addCallback name: " + name + ", device: " + device + ", file: " + callback.getWatchFile());

    }

    public void removeCallback(String name) {
        Iterator it = watchers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            Watcher watcher = (Watcher) entry.getValue();

            if (watcher.getCallbacks().containsKey(name)) {
                watcher.removeCallback(name);
                if (watcher.getCallbacks().isEmpty()) {
                    watcher.stopWatching();
                    it.remove();
                }
            }
        }

        Log.d(TAG, "removeCallback name: " + name);
    }

    // FIXME колбэк может быть удален пока выполняется эта функция
    private void processFile(String path) {
        path = path.replace("//", "/");
        String device = path.replace(Init.DEVICES_DIR, "/").split("/")[1];

        Log.d(TAG, "File modified, device: " + device + ", path: " + path);

        Watcher watcher = watchers.get(device);
        if (watcher == null)
            return;

        // FIXME Unhandled exception in FileObserver org.antrack.app.libs.RecursiveFileObserver$SingleFileObserver@5850edd
        for (Callback callback : watcher.getCallbacks().values()) {
            // проблема в том, что к моменту вызова callback'а фрагмент просто может быть еще не загружен
            if (callback.getWatchFile() != null) {
                if (path.contains(callback.getWatchFile())) {
                    callback.onFileUpdate(path);
                }
            }
        }
    }
}
