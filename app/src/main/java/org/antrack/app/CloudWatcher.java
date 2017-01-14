package org.antrack.app;

import org.antrack.app.libs.L;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// Watch for control file changes in cloud
public class CloudWatcher {
    private static volatile CloudWatcher instance;
    public static CloudWatcher getInstance() {
        CloudWatcher localInstance = instance;
        if (localInstance == null) {
            synchronized (CloudWatcher.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new CloudWatcher();
                }
            }
        }
        return localInstance;
    }

    private final String TAG = "CloudWatcher";

    private HashMap<String, Watcher> watchers;

    private CloudWatcher() {
        watchers = new HashMap<>();
    }


    public interface Callback {
        void onFileUpdate(String path);

        String getWatchFile();
    }

    private class Watcher {
        private HashMap<String, Callback> callbacks;
        private boolean active = false;
        private String device;

        Watcher(String dev) {
            this.device = dev;
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

        void startWatching() {
            active = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    L.d(TAG, "Start thread for device: " + device);

                    while (active) {
                        try {
                            Pw pw = Pw.getInstance();
                            // Sleep if there are no internet connection
                            pw.waitOnline();
                            ArrayList<String> changedFiles = pw.watchForChanges("/" + device);

                            // Second check if thread become inactive while blocked
                            if (!active)
                                break;

                            if (changedFiles != null) {
                                for (String path : changedFiles) {
                                    processFile(path);
                                }
                            }
                        } catch (Exception e) {
                            L.e(TAG, "Thread interrupted");
                            break;
                        }
                    }
                }
            }).start();
        }

        void stopWatching() {
            active = false;
        }
    }

    public void addCallback(String name, Callback callback) {
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
            // Remove "old" key (for fragments)
            /*
            if (watchers.get(device).getCallbacks().containsKey(name))
                watchers.get(device).getCallbacks().remove(name);
                */
            watchers.get(device).addCallback(name, callback);
        }

        L.d(TAG, "addCallback name: " + name + ", device: " + device + ", file: " + callback.getWatchFile());
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

        L.d(TAG, "removeCallback name: " + name);

    }

    private void processFile(String path) {
        String device = path.split("/")[1];

        L.d(TAG, "File modified, device: " + device + ", path: " + path);

        Watcher watcher = watchers.get(device);
        if (watcher == null)
            return;

        for (Callback callback : watchers.get(device).getCallbacks().values()) {
            if (path.contains(callback.getWatchFile())) {
                callback.onFileUpdate(path);
            }
        }
    }
}

