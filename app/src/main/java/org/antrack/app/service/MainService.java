package org.antrack.app.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.splunk.mint.Mint;

import org.antrack.app.C;
import org.antrack.app.CloudWatcher;
import org.antrack.app.Features;
import org.antrack.app.FileWatcher;
import org.antrack.app.Init;
import org.antrack.app.Keys;
import org.antrack.app.OSignal;
import org.antrack.app.Pw;
import org.antrack.app.Settings;
import org.antrack.app.Trial;
import org.antrack.app.libs.Checks;
import org.antrack.app.libs.L;
import org.antrack.app.libs.Utils;
import org.antrack.app.libs.WakeLocks;

import java.io.IOException;

public class MainService extends Service {
    final String TAG = "MainService";

    Context context;

    FileWatcher fileWatcher;
    CloudWatcher cloudWatcher;
    Init init;
    Settings settings;
    Pw pw;
    CC cc;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        /*** Init ***/

        // Error reporting
        Mint.initAndStartSession(MainService.this, "8af105a4");

        init = Init.getInstance();
        settings = Settings.getInstance();
        pw = Pw.getInstance();
        cc = new CC(context);

        new Thread(new Runnable() {
            @Override
            public void run() {
                /*** Check trial and integrity ***/
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!Trial.checkTrial()) {
                            //System.exit(-1);
                            L.e(TAG, "Trial is expired");
                        }
                        // Crash app
                        if (!Checks.all(MainService.this)) {
                            //Pw zz = null;
                            //zz.isConnected();
                            L.e(TAG, "Checks failed");
                        }

                    }
                }).start();

                /*** Set alarm timer ***/

                String updateInterval = settings.get(C.S_UPDATE_INTERVAL);
                long time = Long.parseLong(updateInterval) * 60 * 1000;
                Alarm.set(context, time);

                /*** Start watching for local file changes ***/

                // File watcher must be started AFTER creating all catalogs
                // Catalogs for modules created on load step
                cc.runModules("load", null);

                startFileWatcher();

                // Wait for file watcher
                // FIXME
                Utils.sleep(1);

                /*** Write device features ***/

                Features feat = new Features();
                feat.write(MainService.this, init.MAIN_DIR + C.FEATURES_FILE);

                /*** Write OneSignal Id and generate key pair ***/

                OSignal.writeId();
                Keys.saveKeys();

                /*** Bootstrap ***/

                // Generate /modules file
                cc.parseCommand("!modules");
                cc.parseBootstrap();

                /*** Unpack assets ***/

                Utils.unpackAsset(context, C.ALARM_ASSET);

                /*** Get and watch for clt / ctlq ***/

                startCtlWatching();

                Logger.started(MainService.this);
            }
        }).start();
    }

    private void startFileWatcher() {
        fileWatcher = FileWatcher.getInstance();
        fileWatcher.addCallback("service", new LocalFileUpdated());
    }

    private void stopFileWatcher() {
        if (fileWatcher != null) {
            fileWatcher.removeCallback("service");
        }
    }

    private void startCtlWatching() {
        String ctlEnabled = settings.get(C.S_ENABLE_CTL);
        if (C.TRUE.equals(ctlEnabled)) {
            try {
                U.getFile(C.CONTROL_Q_FILE);
                cloudWatcher = CloudWatcher.getInstance();
                cloudWatcher.addCallback("service", new CloudFileUpdated());
            } catch (Exception e) {
                L.d(TAG, "Can't parse ctlq file: " + e);
            }
        }
    }

    private void stopCtlWatching() {
        String ctlEnabled = settings.get(C.S_ENABLE_CTL);
        if (C.TRUE.equals(ctlEnabled)) {
            if (cloudWatcher != null) {
                cloudWatcher.removeCallback("service");
            }
        }
    }

    // Callback waits for local file changes
    public class LocalFileUpdated implements FileWatcher.Callback {
        public void onFileUpdate(String path) {
            // Current device ctl changed -> read and execute command
            if (path.endsWith(C.CONTROL_FILE)) {
                try {
                    U.parseCtl(cc);
                } catch (IOException e) {
                    L.e(TAG, "Can't read ctl file: " + e.toString());
                }
            }

            // Current device ctlq changed -> read and execute commands
            else if (path.endsWith(C.CONTROL_Q_FILE)) {
                String ctlEnabled = settings.get(C.S_ENABLE_CTL);
                if (C.TRUE.equals(ctlEnabled)) {
                    try {
                        U.parseCtlq(cc);
                    } catch (IOException e) {
                        L.e(TAG, "Can't read ctlq file: " + e.toString());
                    }
                }
            }

            // Other file changed -> upload to cloud
            else {
                try {
                    String isUploaderEnabled = settings.get(C.S_ENABLE_UPLOADER);
                    if (isUploaderEnabled == null || C.TRUE.equals(isUploaderEnabled)) {
                        U.uploadFile(path);
                    }
                } catch (InterruptedException e) {
                    L.e(TAG, "processFile interrupted: " + e.toString());
                }
            }
        }

        public String getWatchFile() {
            return "/" + init.DEVICE_NAME_IMEI + "/";
        }
    }

    // Callback waits for ctl & ctlq changes
    public class CloudFileUpdated implements CloudWatcher.Callback {
        public void onFileUpdate(String path) {
            try {
                Pw pw = Pw.getInstance();
                if (pw.isConnected())
                    pw.getFile(init.DEVICES_DIR + path, path);
            } catch (Exception e) {
                L.e(TAG, "CloudFileUpdated exception: " + e);
            }
        }

        public String getWatchFile() {
            return "/" + init.DEVICE_NAME_IMEI + C.CONTROL_FILE;
        }
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                WakeLocks wl = new WakeLocks(MainService.this, "ServiceWakelock");
                wl.lock();

                if (intent != null) {
                    String action = intent.getAction();
                    L.d(TAG, "onStartCommand get: " + action);
                    if (action != null) {
                        switch (action) {
                            case C.ACTION_BOOT:
                                cc.runModules(C.ACTION_BOOT, null);
                                break;
                            case C.ACTION_ALARM:
                                Logger.alarm(context);
                                cc.runModules(C.ACTION_ALARM, null);
                                break;
                            case C.ACTION_SCREENON:
                                cc.runModules(C.ACTION_SCREENON, null);
                                break;
                            case C.ACTION_OUTGOINGCALL:
                                String outNumber = intent.getStringExtra("phoneNumber");
                                if (outNumber != null)
                                    cc.runModules(C.ACTION_OUTGOINGCALL, outNumber);
                                break;
                            case C.ACTION_INCOMINGCALL:
                                String number = intent.getStringExtra("phoneNumber");
                                if (number != null)
                                    cc.runModules(C.ACTION_INCOMINGCALL, number);
                                break;
                            case C.ACTION_COMMAND:
                                cc.parseCommand(intent.getStringExtra("command"));
                                break;
                            case C.ACTION_CTL_ENABLED:
                                startCtlWatching();
                                break;
                            case C.ACTION_CTL_DISABLED:
                                stopCtlWatching();
                                break;
                            case C.ACTION_PUSH:
                                try {
                                    /*
                                    pw.getFile(Init.DEVICES_DIR + Init.DEVICE_NAME_IMEI + C.CONTROL_Q_FILE,
                                            "/" + Init.DEVICE_NAME_IMEI + C.CONTROL_Q_FILE);
                                    U.parseCtlq(cc);
                                    */
                                    String cmd = intent.getStringExtra("command");
                                    if (cmd != null) {
                                        cc.parseCommand(cmd);
                                    }
                                } catch (Exception e) {
                                    L.d(TAG, "Error getting file: " + e.toString());
                                }
                                break;
                            }
                        }
                }
                // Wait other threads
                Utils.sleep(5);
                wl.unlock();
            }
        }).start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopFileWatcher();
        stopCtlWatching();

        String enabled = settings.get(C.S_ENABLE_SERVICE);
        if (enabled == null || enabled.equals("false")) {
            Alarm.cancel(this);
        }

        Logger.stopped(this);

        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
