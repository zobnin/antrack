package org.antrack.app;

import android.content.Context;

import org.antrack.app.libs.Files;
import org.antrack.app.libs.L;
import org.antrack.app.libs.Shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Settings {
    private static final String TAG = "Settings";

    private static Properties prop = null;
    private static String settingsFile = null;

    public static boolean needLaunchWizard(Context context) {
        String wizardCompleteFile = context.getApplicationInfo().dataDir + C.WIZARD_COMPLETE_FILE;
        return (!new File(wizardCompleteFile).exists());
    }

    public static void wizardComplete(Context context) {
        String wizardCompleteFile = context.getApplicationInfo().dataDir + C.WIZARD_COMPLETE_FILE;
        Files.touch(wizardCompleteFile);
    }

    public static void saveToken(String token) {
        try {
            Files.writeTextFile(Init.APP_DIR + C.TOKEN_FILE, token);
        } catch (IOException e) {
            L.e(TAG, "Can't save token: " + e.toString());
        }
    }

    public static String readToken() {
        String token = null;

        try {
            token = Files.readTextFile(Init.APP_DIR + C.TOKEN_FILE);
        } catch (IOException e) {
            L.e(TAG, "Can't read token: " + e.toString());
        }

        return token;
    }

    public static void init() {
        if (prop == null) {
            settingsFile = Init.MAIN_DIR + C.SETTINGS_FILE;
            Shell.runCommand("touch " + settingsFile);
            load();
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

}
