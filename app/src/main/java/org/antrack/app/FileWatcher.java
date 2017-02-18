package org.antrack.app;

import org.antrack.app.libs.L;
import org.antrack.app.libs.RecursiveFileObserver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileWatcher {
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

    private static final String TAG = "FileWatcher";

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
        // ConcurrentModificationException workaround
        private ConcurrentHashMap<String, Callback> callbacks;

        Watcher(String device) {
            super(Init.getInstance().DEVICES_DIR + device);
            callbacks = new ConcurrentHashMap<>();
        }

        ConcurrentHashMap<String, Callback> getCallbacks() {
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

    public synchronized void addCallback(String name, Callback callback) {
        if (callback == null) {
            L.e(TAG, "addCallback: callback == null");
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

        L.d(TAG, "addCallback name: " + name + ", device: " + device + ", file: " + callback.getWatchFile());

    }

    public synchronized void removeCallback(String name) {
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

        L.d(TAG, "removeCallback name: " + name);
    }

    // FIXME штука в том, что callback может быть добавлен/удален пока выполняется processFile
    private void processFile(String path) {
        path = path.replace("//", "/");
        String device = path.replace(Init.getInstance().DEVICES_DIR, "/").split("/")[1];

        L.d(TAG, "File modified, device: " + device + ", path: " + path);

        Watcher watcher = watchers.get(device);
        if (watcher == null)
            return;

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
