package org.antrack.app;

import org.antrack.app.libs.L;
import org.antrack.app.libs.Shell;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class Settings {
    private static final String TAG = "Settings";

    private static Properties prop = null;
    private static String settingsFile = null;

    public static void init() {
        if (prop == null) {
            settingsFile = Init.MAIN_DIR + C.SETTINGS_FILE;
            Shell.runCommand("touch " + settingsFile);
            load();
        }
    }

    private static void load() {
        try {
            prop = new Properties();
            prop.load(new FileInputStream(settingsFile));
        } catch (Exception e) {
            L.e(TAG, e.toString());
        }
    }

    private static void store() {
        try {
            prop.store(new FileOutputStream(settingsFile), "");
        } catch (Exception e) {
            L.e(TAG, e.toString());
        }
    }

    public static void put(String name, String value) {
        prop.setProperty(name, value);
        store(); // For cloud synchronization
        L.d(TAG, "Set settings: " + name + " = " + value);
    }

    public static String get(String name) {
        String value = prop.getProperty(name);
        L.d(TAG, "Get settings: " + name + " = " + value);
        return value;
    }
}
