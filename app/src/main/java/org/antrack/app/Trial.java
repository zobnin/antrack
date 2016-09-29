package org.antrack.app;

import android.os.Environment;
import android.util.Log;

import org.antrack.app.libs.Files;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class Trial {
    // Compare saved date with current date
    // Save date if there are no saved date
    public static boolean checkDate() {
        long currentDate = getCurrentDate();
        long savedDate = getSavedDate();

        if (savedDate < 0) {
            saveDate();
            return true;
        }

        if (currentDate < 0) {
            return true;
        }

        long elapsedDays = (currentDate - savedDate) / 1000 / 60 / 60 / 24;

        Log.d("TRIAL", "elapsedDays: " + elapsedDays);

        // Little trick to make reversing harder
        return elapsedDays < C.CONTROL_Q_MAX_LENGTH;
    }

    // Save current date
    // Current date written to three different places:
    // * Main dir (file: "index_date")
    // * Cloud disk (file: "index_date")
    // * SD card (file: ".cache")
    private static void saveDate() {
        long date = getCurrentDate();
        if (date < 0)
            return;

        String lFile = Init.APP_DIR + "/index_" + date;
        String rFile = "/index_" + date;
        String sdFile = Environment.getExternalStorageDirectory() + "/.cache";

        try {
            Files.writeTextFile(lFile, String.valueOf(date));
            Pw pw = Pw.getInstance();
            pw.putFile(lFile, rFile, true);
            pw.delete(rFile, true);
            // Last because it may cause exception
            Files.writeTextFile(sdFile, String.valueOf(date));
        } catch(Exception e) {
            Log.e("TRIAL", "saveDate exception: " + e.toString());
        }
    }

    private static long getSavedDate() {
        long date1 = getDateFromAppDir();
        long date2 = getDateFromSdcard();
        long date3 = getDateFromCloud();

        if (date2 > date1) return date2;
        if (date3 > date2 || date3 > date1) return date3;
        return date1;
    }

    private static long getDateFromAppDir() {
        long date = -1;
        try {
            String[] localFiles = new File(Init.APP_DIR).list();
            for (String file : localFiles) {
                if (file.startsWith("/index_")) {
                    date = Long.parseLong(file.substring(file.indexOf('_') + 1));
                }
            }
        } catch (Exception e) {
            Log.e("TRIAL", "getDateFromAppDir exception: " + e);
        }

        Log.d("TRIAL", "getDateFromAppDir: " + date);

        return date;
    }

    private static long getDateFromSdcard() {
        long date = -1;
        try {
            date = Long.parseLong(Files.readTextFile(
                    Environment.getExternalStorageDirectory() + "/.cache"));
        } catch (Exception e) {
            Log.e("TRIAL", "getDateFromSdcard exception: " + e);
        }

        Log.d("TRIAL", "getDateFromSdcard: " + date);

        return date;
    }

    private static long getDateFromCloud() {
        long date = -1;
        try {
            Pw pw = Pw.getInstance();
            ArrayList<String> cloudFiles = pw.listDir("", true);
            for (String file : cloudFiles) {
                Log.e("DEBUG", "file: " + file);
                if (file.startsWith("/index_")) {
                    date = Long.parseLong(file.substring(file.indexOf('_') + 1));
                }
            }
        } catch (Exception e) {
            Log.e("TRIAL", "getDateFromCloud exception: " + e);
        }

        Log.d("TRIAL", "getDateFromCloud: " + date);

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
            Log.e("TRIAL", "getDate exception: " + e.toString());
        }

        return date;
    }

}
