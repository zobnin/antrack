package org.antrack.app.libs;

import android.os.Environment;
import android.util.Log;

import java.io.IOException;

public class L {
    private static final boolean LOGCAT = true;
    private static final boolean FILE = false;

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
            Files.addLine(Environment.getDataDirectory().getPath() + "/logs",
                    Utils.date("yyyy.MM.dd HH:mm:ss.SSS") + " " + msg);
        } catch (IOException e) {
            Log.e("L", "error: " + e);
        }
    }
}
