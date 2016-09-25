package org.antrack.app;

import android.os.Environment;
import android.util.Log;

import org.antrack.app.libs.Files;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class Trial {
    // FIXME использовать другое число и вычислять TRIAL_TIME на месте
    private static final long TRIAL_TIME = 10;

    public static void checkDate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long currentDate = getCurrentDate();
                long savedDate = getSavedDate();

                if (savedDate < 0) {
                    saveDate();
                    return;
                }

                if (currentDate < 0) {
                    return;
                }

                long elapsedDays = (currentDate - savedDate) / 1000 / 60 / 60 / 24;

                Log.e("DEBUG", "elapsedDays: " + elapsedDays);

                if (elapsedDays > TRIAL_TIME) {
                    // FIXME завершаемся
                }
            }
        }).start();
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

        String lFile = Init.MAIN_DIR + "index_" + date;
        String rFile = "/index_" + date;
        String sdFile = Environment.getExternalStorageState() + "/.cache";

        try {
            Files.writeTextFile(lFile, String.valueOf(date));
            Files.writeTextFile(sdFile, String.valueOf(date));
            Pw pw = Pw.getInstance();
            pw.putFile(lFile, rFile, true);
            pw.delete(rFile, true);
        } catch(Exception e) {
            Log.e("Trial", "saveDate exception: " + e.toString());
        }
    }

    private static long getSavedDate() {
        long date = -1;
        try {
            // Check app folder
            String[] localFiles = new File(Init.MAIN_DIR).list();
            for (String file : localFiles) {
                if (file.startsWith("/index_")) {
                    date = Long.parseLong(file.substring(file.indexOf('_') + 1));
                }
            }

            // Check /sdcard
            long date2 = Long.parseLong(Files.readTextFile(Environment.getExternalStorageState() + "/.cache"));
            if (date2 > date)
                date = date2;

            // Check cloud disk
            long date3 = -1;
            Pw pw = Pw.getInstance();
            ArrayList<String> cloudFiles = pw.listDir("", true);
            for (String file : cloudFiles) {
                Log.e("DEBUG", "file: " + file);
                if (file.startsWith("/index_")) {
                    date3 = Long.parseLong(file.substring(file.indexOf('_') + 1));
                }
            }
            if (date3 > date)
                date = date3;

            Log.e("DEBUG", "checkDate: " + date);
        } catch (Exception e) {
            Log.e("Trial", "checkDate exception: " + e.toString());
        }
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
            Log.e("Trial", "getDate exception: " + e.toString());
        }

        return date;
    }

}
