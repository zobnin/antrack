package org.antrack.app;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Watch for control file changes in cloud
public class CloudWatcher {
    private final String TAG = "CloudWatcher";

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

    ExecutorService executor;
    HashMap<String, Watcher> watchers;

    public CloudWatcher() {
        executor = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
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

        public Watcher(String dev) {
            this.device = dev;
            callbacks = new HashMap<>();

        }

        public HashMap<String, Callback> getCallbacks() {
            return callbacks;
        }

        public void addCallback(String name, Callback callback) {
            callbacks.put(name, callback);
        }

        public void removeCallback(String name) {
            callbacks.remove(name);
        }

        public void startWatching() {
            active = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Start thread for device: " + device);

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
                            Log.e(TAG, "Thread interrupted");
                            break;
                        }
                    }
                }
            }).start();
        }

        public void stopWatching() {
            active = false;
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
            // Remove "old" key (for fragments)
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
                    // FIXME в разном порядке
                    watcher.stopWatching();
                    it.remove();
                }
            }
        }

        Log.d(TAG, "removeCallback name: " + name);

    }

    private void processFile(String path) {
        String device = path.split("/")[1];

        Log.d(TAG, "File modified, device: " + device + ", path: " + path);

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

