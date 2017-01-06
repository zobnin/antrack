package org.antrack.app.libs;

import android.util.Log;

import java.io.IOException;

public class L {
    private static final boolean LOGCAT = true;
    private static final boolean FILE = true;

    public static void d(String tag, String msg) {
        if (LOGCAT) { Log.d(tag, msg); }
        if (FILE) { writeToFile(tag + ": " + msg); }
    }

    public static void e(String tag, String msg) {
        if (LOGCAT) { Log.e(tag, msg); }
        if (FILE) { writeToFile(tag + ": " + msg); }
    }

    private static void writeToFile(String msg) {
        try {
            Files.addLine("/sdcard/logs", Utils.date("yyyy.MM.dd HH:mm:ss.SSS") + " " + msg);
        } catch (IOException e) {
            Log.e("L", "error: " + e);
        }
    }
}
