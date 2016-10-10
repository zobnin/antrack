package org.antrack.app.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.splunk.mint.Mint;

import org.antrack.app.C;
import org.antrack.app.CloudWatcher;
import org.antrack.app.FileWatcher;
import org.antrack.app.Init;
import org.antrack.app.Pw;
import org.antrack.app.Settings;
import org.antrack.app.Trial;
import org.antrack.app.libs.Checks;
import org.antrack.app.libs.Utils;

import java.io.File;
import java.io.IOException;

public class MainService extends Service {
    final String TAG = "MainService";

    FileWatcher fileWatcher;
    CloudWatcher cloudWatcher;
    Context context;
    Pw pw;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        new Thread(new Runnable() {
            @Override
            public void run() {
                /*** Init ***/

                // Error reporting
                Mint.initAndStartSession(MainService.this, "8af105a4");

                // Dirs and settings
                Init.all(context);
                // Cloud connection
                pw = Pw.getInstance();
                // Control Center
                V.cc = new CC(context);

                /*** Check trial and integrity ***/

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!Trial.checkTrial()) {
                            //System.exit(-1);
                            Log.e(TAG, "Trial is expired");
                        }
                        // Crash app
                        if (!Checks.all(MainService.this)) {
                            //Pw zz = null;
                            //zz.isConnected();
                            Log.e(TAG, "Checks failed");
                        }

                    }
                }).start();

                /*** Set alarm timer ***/

                String updateInterval = Settings.get(C.S_UPDATE_INTERVAL);
                long time = Long.parseLong(updateInterval) * 60 * 1000;
                Alarm.set(context, time);

                /*** Start watching for local file changes ***/

                fileWatcher = FileWatcher.getInstance();
                fileWatcher.addCallback("service", new LocalFileUpdated());

                // Wait for file watcher
                // FIXME
                Utils.sleep(1);

                /*** Bootstrap modules ***/

                // Generate /modules file
                if (!new File(Init.MAIN_DIR + C.MODULES_FILE).exists()) {
                    V.cc.parseCommand("!modules");
                }

                V.cc.runModules("load", null);
                V.cc.parseBootstrap();

                /*** Get ctlq ***/

                try {
                    U.getFile(C.CONTROL_Q_FILE);
                } catch (Exception e) {
                    Log.d(TAG, "Can't parse ctlq file: " + e);
                }

                /*** Watch for remote file changes ***/

                cloudWatcher = CloudWatcher.getInstance();
                cloudWatcher.addCallback("service", new CloudFileUpdated());

                Logger.started(MainService.this);
            }
        }).start();
    }

    // Callback waits for local file changes
    public class LocalFileUpdated implements FileWatcher.Callback {
        public void onFileUpdate(String path) {
            // Current device ctl changed -> read and execute command
            if (path.endsWith(C.CONTROL_FILE)) {
                try {
                    U.parseCtl();
                } catch (IOException e) {
                    Log.e(TAG, "Can't read ctl file: " + e.toString());
                }
            }
            // Current device ctlq changed -> read and execute commands
            else if (path.endsWith(C.CONTROL_Q_FILE)) {
                try {
                    U.parseCtlq();
                } catch (IOException e) {
                    Log.e(TAG, "Can't read ctlq file: " + e.toString());
                }
            }
            // Other file changed -> upload to cloud
            else {
                try {
                    U.uploadFile(path);
                } catch (InterruptedException e) {
                    Log.e(TAG, "processFile interrupted: " + e.toString());
                }
            }
        }

        public String getWatchFile() {
            return "/" + Init.DEVICE_NAME_IMEI + "/";
        }
    }

    // Callback waits for /control changes
    public class CloudFileUpdated implements CloudWatcher.Callback {
        public void onFileUpdate(String path) {
            try {
                Pw pw = Pw.getInstance();
                if (pw.isConnected())
                    pw.getFile(Init.DEVICES_DIR + path, path);
            } catch (Exception e) {
                Log.e(TAG, "CloudFileUpdated exception: " + e);
            }
        }

        public String getWatchFile() {
            return "/" + Init.DEVICE_NAME_IMEI + C.CONTROL_FILE;
        }
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onStartCommmand");

                if (intent != null) {
                    String action = intent.getAction();
                    if (action != null) {
                        switch (action) {
                            case "boot":
                                Log.d(TAG, "Get boot");
                                V.cc.runModules("boot", null);
                                break;
                            case "alarm":
                                Log.d(TAG, "Get alarm");
                                Logger.alarm(context);
                                V.cc.runModules("alarm", null);
                                break;
                            case "screenOn":
                                Log.d(TAG, "Get screenOn");
                                V.cc.runModules("screenOn", null);
                                break;
                            case "outgoingCall":
                                Log.d(TAG, "Get outgoingCall");
                                String outNumber = intent.getStringExtra("phoneNumber");
                                if (outNumber != null)
                                    V.cc.runModules("outgoingCall", outNumber);
                                break;
                            case "incomingCall":
                                Log.d(TAG, "Get incomingCall");
                                String number = intent.getStringExtra("phoneNumber");
                                if (number != null)
                                    V.cc.runModules("incomingCall", number);
                                break;
                            case "command":
                                Log.d(TAG, "Get command");
                                V.cc.parseCommand(intent.getStringExtra("command"));
                                break;
                            }
                        }
                }
            }
        }).start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        fileWatcher.removeCallback("service");
        cloudWatcher.removeCallback("service");

        // FIXME проблема в том, что если сервис отключен из-за отсутствия интернета alarm будет его запускать
//        String enabled = Settings.get(C.S_ENABLE_SERVICE);
//        if (enabled == null || enabled.equals("false")) {
            Alarm.cancel(this);
//        }

        Logger.stopped(this);

        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
