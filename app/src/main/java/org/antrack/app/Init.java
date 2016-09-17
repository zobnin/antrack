package org.antrack.app;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.antrack.app.libs.Files;
import org.antrack.app.libs.Shell;
import org.antrack.app.libs.Utils;

import java.io.IOException;

public class Init {
    private static String TAG="Init";

    private static boolean done = false;

    // This device name
    public static String DEVICE_NAME;
    // This device IMEI
    public static String DEVICE_IMEI;
    // This device actual name in app
    public static String DEVICE_NAME_IMEI;

    // App directory
    public static String APP_DIR;
    // App directory + devices
    public static String DEVICES_DIR;
    // App directory + devices + main device name
    public static String MAIN_DIR;

    // Full paths to control and result file
    public static String CONTROL_FILE;
    public static String CONTROL_Q_FILE;
    public static String RESULT_FILE;

    public static void all(Context context) {
        if (!done) {
            Log.d(TAG, "Initialization...");
            getIMEI(context);
            makeDirs(context);
            initSettings(context);
            initLastCmdTime();
            writeName();
            done = true;
        }
    }

    public static void makeDirs(Context context) {
        APP_DIR = context.getApplicationInfo().dataDir;
        DEVICES_DIR = APP_DIR + C.DEVICES_DIR;

        DEVICE_NAME = android.os.Build.MODEL.toLowerCase();
        DEVICE_NAME_IMEI = DEVICE_NAME + "_" + DEVICE_IMEI.substring(DEVICE_IMEI.length() - 4);

        MAIN_DIR = DEVICES_DIR + DEVICE_NAME_IMEI;

        Log.d(TAG, "Device dir: " + MAIN_DIR);

        CONTROL_FILE = MAIN_DIR + C.CONTROL_FILE;
        CONTROL_Q_FILE = MAIN_DIR + C.CONTROL_Q_FILE;
        RESULT_FILE = MAIN_DIR + C.RESULT_FILE;

        // TODO Нормально создать каталоговую структуру
        Shell.runCommand("mkdir -p " + MAIN_DIR);
        Shell.runCommand("touch " + CONTROL_FILE);
        Shell.runCommand("touch " + CONTROL_Q_FILE);
    }

    public static void getIMEI(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        DEVICE_IMEI = tm.getDeviceId();
    }

    public static void writeName() {
        try {
            Files.writeTextFile(MAIN_DIR + C.NAME_FILE,
                    android.os.Build.BRAND + " " + android.os.Build.MODEL);
        } catch (IOException e) {
            Log.e(TAG, "Can't write /name: " + e.toString());
        }
    }

    public static void initSettings(Context context) {
        Settings.init();

        if (Settings.get(C.S_UPDATE_INTERVAL) == null) {
            Settings.put(C.S_UPDATE_INTERVAL, C.UPDATE_INTERVAL);
        }

        if (Settings.get(C.S_IMSI) == null) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String IMSI = tm.getSubscriberId();
            if (IMSI != null) {
                Settings.put(C.S_IMSI, IMSI);
            } else {
                Log.e(TAG, "Can't get IMSI");
            }
        }
    }

    public static void initLastCmdTime() {
        if (Settings.get(C.S_LAST_CMD_TIME) == null) {
            String currentTime = Utils.date(C.LAST_CMD_TIME_FORMAT);
            Settings.put(C.S_LAST_CMD_TIME, currentTime);
        }
    }
}
