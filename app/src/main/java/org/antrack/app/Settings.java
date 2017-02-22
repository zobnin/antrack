package org.antrack.app;

import android.content.Context;
import android.telephony.TelephonyManager;

import org.antrack.app.libs.Files;
import org.antrack.app.libs.L;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Settings {
    private static volatile Settings instance;
    public static Settings getInstance() {
        Settings localInstance = instance;
        if (localInstance == null) {
            synchronized (Settings.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new Settings();
                }
            }
        }
        return localInstance;
    }

    private static final String TAG = "Settings";

    private Context context;

    private Properties prop = null;
    private String settingsFile = null;

    public Settings() {
        context = AntrackApplication.getAppContext();

        settingsFile = getLocalDeviceDir() + C.SETTINGS_FILE;
        Files.touch(settingsFile);

        try {
            prop = new Properties();
            prop.load(new FileInputStream(settingsFile));
        } catch (Exception e) {
            L.e(TAG, e.toString());
        }
    }


    public static boolean needLaunchWizard() {
        String wizardCompleteFile = AntrackApplication.getAppContext()
                .getApplicationInfo().dataDir + C.WIZARD_COMPLETE_FILE;
        return !new File(wizardCompleteFile).exists();
    }

    public static void wizardComplete() {
        String wizardCompleteFile = AntrackApplication.getAppContext()
                .getApplicationInfo().dataDir + C.WIZARD_COMPLETE_FILE;
        Files.touch(wizardCompleteFile);
    }

    public void saveToken(String token) {
        try {
            Files.writeTextFile(context.getApplicationInfo().dataDir + C.TOKEN_FILE, token);
        } catch (IOException e) {
            L.e(TAG, "Can't save token: " + e.toString());
        }
    }

    public String readToken() {
        String token = null;

        try {
            token = Files.readTextFile(context.getApplicationInfo().dataDir + C.TOKEN_FILE);
        } catch (IOException e) {
            L.e(TAG, "Can't read token: " + e.toString());
        }

        return token;
    }

    public void put(String name, String value) {
        prop.setProperty(name, value);
        store(); // For cloud synchronization
        L.d(TAG, "Set settings: " + name + " = " + value);
    }

    public String get(String name) {
        String value = prop.getProperty(name);
        L.d(TAG, "Get settings: " + name + " = " + value);
        return value;
    }

    private void store() {
        try {
            prop.store(new FileOutputStream(settingsFile), "");
        } catch (Exception e) {
            L.e(TAG, e.toString());
        }
    }

    // Settings dir = Init.MAIN_DIR
    private String getLocalDeviceDir() {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String DeviceName = android.os.Build.MODEL.toLowerCase().replace(" ", "_");
        String IMEI = tm.getDeviceId();

        return context.getApplicationInfo().dataDir + C.DEVICES_DIR +
                "/" + DeviceName + "_" + IMEI.substring(IMEI.length() - 4);
    }
}
