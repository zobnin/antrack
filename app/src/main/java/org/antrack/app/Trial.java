package org.antrack.app;

import android.os.Environment;

import org.antrack.app.libs.Files;
import org.antrack.app.libs.L;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class Trial {
    private static final String SDCARD_FILE = "/image_cache";

    // Compare saved date with current date
    // Save date if there are no saved date
    public static boolean checkTrial() {
        long remainingDays = getRemainingDays();

        L.d("TRIAL", "remainingDays: " + remainingDays);

        return remainingDays > 0;
    }

    public static long getRemainingDays() {
        long currentDate = getCurrentDate();
        long savedDate = getSavedDate();

        if (savedDate < 0) {
            saveDate();
            return -1;
        }

        if (currentDate < 0) {
            return -1;
        }

        // Little trick to make reversing harder
        return C.CONTROL_Q_MAX_LENGTH - (currentDate - savedDate) / 1000 / 60 / 60 / 24;
    }

    // Save current date
    // Current date written to three different places:
    // * Main dir (file: "index_DATE")
    // * Cloud disk (file: "index_DATE")
    // * SD card (file: "image_cache")
    private static void saveDate() {
        long date = getCurrentDate();
        if (date < 0)
            return;

        String lFile = Init.APP_DIR + "/index_" + date;
        String rFile = "/index_" + date;
        String sdFile = Environment.getExternalStorageDirectory() + SDCARD_FILE;

        try {
            Files.writeTextFile(lFile, String.valueOf(date));
            Files.writeTextFile(sdFile, String.valueOf(date));

            Pw pw = Pw.getInstance();
            pw.putFile(lFile, rFile, true);
            pw.delete(rFile, true);

            // Last because it may cause exception
            Files.writeTextFile(sdFile, String.valueOf(date));
        } catch(Exception e) {
            L.e("TRIAL", "saveDate exception: " + e.toString());
        }
    }

    private static long getSavedDate() {
        long[] dates = new long[3];

        dates[0] = getDateFromAppDir();
        dates[1] = getDateFromSdcard();
        dates[2] = getDateFromCloud();

        long min = dates[0];
        for (long i : dates) {
            if (i < min) min = i;
        }

        return min;
    }

    private static long getDateFromAppDir() {
        long date = Long.MAX_VALUE;
        try {
            String[] localFiles = new File(Init.APP_DIR).list();
            for (String file : localFiles) {
                if (file.startsWith("/index_")) {
                    date = Long.parseLong(file.substring(file.indexOf('_') + 1));
                }
            }
        } catch (Exception e) {
            L.e("TRIAL", "getDateFromAppDir exception: " + e);
        }

        L.d("TRIAL", "getDateFromAppDir: " + date);

        return date;
    }

    private static long getDateFromSdcard() {
        long date = Long.MAX_VALUE;
        try {
            date = Long.parseLong(Files.readTextFile(
                    Environment.getExternalStorageDirectory() + SDCARD_FILE));
        } catch (Exception e) {
            L.e("TRIAL", "getDateFromSdcard exception: " + e);
        }

        L.d("TRIAL", "getDateFromSdcard: " + date);

        return date;
    }

    private static long getDateFromCloud() {
        long date = Long.MAX_VALUE;
        try {
            Pw pw = Pw.getInstance();
            ArrayList<String> cloudFiles = pw.listDir("", true);
            for (String file : cloudFiles) {
                if (file.startsWith("/index_")) {
                    date = Long.parseLong(file.substring(file.indexOf('_') + 1));
                }
            }
        } catch (Exception e) {
            L.e("TRIAL", "getDateFromCloud exception: " + e);
        }

        L.d("TRIAL", "getDateFromCloud: " + date);

        return date;
    }

    // Get date from HTTP header
    private static long getCurrentDate() {
        long date = -1;
        try {
            URL url = new URL("http://google.com");
            URLConnection conn = url.openConnection();
            date = conn.getDate();
        } catch (Exception e) {
            L.e("TRIAL", "getDate exception: " + e.toString());
        }

        return date;
    }

}
