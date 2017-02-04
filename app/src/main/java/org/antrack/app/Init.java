package org.antrack.app;

import android.content.Context;
import android.telephony.TelephonyManager;

import org.antrack.app.libs.Files;
import org.antrack.app.libs.L;
import org.antrack.app.libs.Utils;

import java.io.IOException;

public class Init {
    private static volatile Init instance;
    public static Init getInstance() {
        Init localInstance = instance;
        if (localInstance == null) {
            synchronized (Init.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new Init();
                }
            }
        }
        return localInstance;
    }

    private static final String TAG="Init";

    private Context context;
    private Settings settings;

    // This device name
    public String DEVICE_NAME;
    // This device IMEI
    public String DEVICE_IMEI;
    // This device actual name in app
    public String DEVICE_NAME_IMEI;

    // App directory
    public String APP_DIR;
    // App directory + devices
    public String DEVICES_DIR;
    // App directory + devices + main device name
    public String MAIN_DIR;

    // Full paths to control and result file
    public String CONTROL_FILE;
    public String CONTROL_Q_FILE;
    public String RESULT_FILE;

    public Init() {
        L.d(TAG, "Initialization...");

        context = AntrackApplication.getAppContext();

        getIMEI(context);
        makeDirs(context);
        initSettings(context);
        initLastCmdTime();
        writeName();
    }

    private void makeDirs(Context context) {
        APP_DIR = context.getApplicationInfo().dataDir;
        DEVICES_DIR = APP_DIR + C.DEVICES_DIR;

        DEVICE_NAME = android.os.Build.MODEL.toLowerCase();
        DEVICE_NAME = DEVICE_NAME.replace(" ", "_");
        DEVICE_NAME_IMEI = DEVICE_NAME + "_" + DEVICE_IMEI.substring(DEVICE_IMEI.length() - 4);

        MAIN_DIR = DEVICES_DIR + DEVICE_NAME_IMEI;

        L.d(TAG, "Device dir: " + MAIN_DIR);

        CONTROL_FILE = MAIN_DIR + C.CONTROL_FILE;
        CONTROL_Q_FILE = MAIN_DIR + C.CONTROL_Q_FILE;
        RESULT_FILE = MAIN_DIR + C.RESULT_FILE;

        Files.mkdirs(MAIN_DIR);
        Files.touch(CONTROL_FILE);
        Files.touch(CONTROL_Q_FILE);
    }

    private void getIMEI(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        DEVICE_IMEI = tm.getDeviceId();
    }

    private void writeName() {
        try {
            Files.writeTextFile(MAIN_DIR + C.NAME_FILE,
                    android.os.Build.BRAND + " " + android.os.Build.MODEL);
        } catch (IOException e) {
            L.e(TAG, "Can't write /name: " + e.toString());
        }
    }

    private void initSettings(Context context) {
        settings = Settings.getInstance();

        if (settings.get(C.S_UPDATE_INTERVAL) == null) {
            settings.put(C.S_UPDATE_INTERVAL, C.UPDATE_INTERVAL);
        }

        if (settings.get(C.S_IMSI) == null) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String IMSI = tm.getSubscriberId();
            if (IMSI != null) {
                settings.put(C.S_IMSI, IMSI);
            } else {
                L.e(TAG, "Can't get IMSI");
            }
        }
    }

    private  void initLastCmdTime() {
        if (settings.get(C.S_LAST_CMD_TIME) == null) {
            String currentTime = Utils.date(C.LAST_CMD_TIME_FORMAT);
            settings.put(C.S_LAST_CMD_TIME, currentTime);
        }
    }
}
