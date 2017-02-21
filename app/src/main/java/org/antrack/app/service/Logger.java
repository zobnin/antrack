package org.antrack.app.service;

import android.content.Context;

import org.antrack.app.C;
import org.antrack.app.Init;
import org.antrack.app.libs.Files;
import org.antrack.app.libs.L;
import org.antrack.app.libs.Shell;
import org.antrack.app.libs.Utils;

import java.io.File;
import java.io.IOException;

public class Logger {
    static private String TAG = "Logger";

    private static void put(String txt) {
        String logfile = Init.getInstance().MAIN_DIR + C.MAIN_LOG_FILE;

        try {
            L.d(TAG, "New record: " + txt);

            File file = new File(logfile);
            if (file.exists()) {
                int ln = Files.countLines(logfile);
                if (ln > C.LOGS_MAX) {
                    Shell.runCommand("mv " + logfile + " " + logfile + ".old");
                }
            }
            Files.addLine(logfile, Utils.date(C.DEFAULT_TIME_FORMAT) + " " + txt);
        } catch (IOException e) {
            L.e(TAG, "put IOException");
        }
    }

    public static void getCommand(Context context, String cmd) {
        put("[info] Get command: " + cmd);
    }

    public static void getPush(String device, String cmd) {
        put("[info] Get command: " + cmd + ", from device: " + device);
    }

    public static void booted(Context context) {
        put("[info] Phone booted");
    }

    public static void shutdown(Context context) {
        put("[info] Phone power off");
    }

    public static void connected(Context context) {
        put("[info] Connected to network");
    }

    public static void disconnected(Context context) {
        put("[info] Disconnected from network");
    }

    public static void started(Context context) {
        put("[info] Service started");
    }

    public static void stopped(Context context) {
        put("[warning] Service stopped");
    }

    public static void hided(Context context) {
        put("[warning] App hided");
    }

    public static void unhided(Context context) {
        put("[info] App unhided");
    }

    public static void lost(Context context) {
        put("[warning] Phone marked as lost");
    }

    public static void unlost(Context context) {
        put("[info] Phone market as not lost");
    }

    public static void simChanged(Context context) {
        put("[warning] SIM Change detected!");
    }

    public static void alarm(Context context) {
        put("[info] Periodic tasks launched");
    }

    public static void cantDecrypt(String deviceName) {
        put("[warning] Can't decrypt message from " + deviceName);
    }

    public static void trusted(String deviceName) {
        put("[info] Device " + deviceName + " now trusted");
    }

    public static void banned(String deviceName) {
        put("[info] Device " + deviceName + " now banned");
    }
}
