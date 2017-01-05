package org.antrack.app.service;

import android.content.Context;
import android.util.Log;

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
    static private String logfile;

    private static void put(String txt) {
        try {
            L.d(TAG, "New record: " + txt);

            File file = new File(logfile);
            if (file.exists()) {
                int ln = Files.countLines(logfile);
                if (ln > C.LOGS_MAX) {
                    Shell.runCommand("mv " + logfile + " " + logfile + ".old");
                }
            }
            Files.addLine(logfile, Utils.date("yyyy.MM.dd HH:mm:ss") + " " + txt);
        } catch (IOException e) {
            L.e(TAG, "put IOException");
        }
    }

    private static void init(Context context) {
        logfile = Init.MAIN_DIR + C.MAIN_LOG_FILE;
    }

    public static void getCommand(Context context, String cmd) {
        init(context);
        put("Get command: " + cmd);
    }

    public static void booted(Context context) {
        init(context);
        put("Phone booted");
    }

    public static void shutdown(Context context) {
        init(context);
        put("Phone power off");
    }

    public static void connected(Context context) {
        init(context);
        put("Connected to network");
    }

    public static void disconnected(Context context) {
        init(context);
        put("Disconnected from network");
    }

    public static void started(Context context) {
        init(context);
        put("Service started");
    }

    public static void stopped(Context context) {
        init(context);
        put("Service stopped");
    }

    public static void hided(Context context) {
        init(context);
        put("App hided");
    }

    public static void unhided(Context context) {
        init(context);
        put("App unhided");
    }

    public static void lost(Context context) {
        init(context);
        put("Phone marked as lost");
    }

    public static void unlost(Context context) {
        init(context);
        put("Phone market as not lost");
    }

    public static void simChanged(Context context) {
        init(context);
        put("SIM Change detected!");
    }

    public static void alarm(Context context) {
        init(context);
        put("Periodic tasks launched");
    }
}
