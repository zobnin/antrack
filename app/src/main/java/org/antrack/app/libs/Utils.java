package org.antrack.app.libs;

import android.content.Context;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    // Not thread safe
    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static String date(String format) {
        DateFormat dateFormat = new SimpleDateFormat(format, Locale.US);
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static void sleep(int time) {
        try {
            Thread.sleep(time * 1000);
        } catch (InterruptedException e) {}
    }

    public static String StreamToString(InputStream is) {
    /*
     * To convert the InputStream to String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static String arrayToString(String ar[]) {
        StringBuilder sb = new StringBuilder();
        for (String s : ar) {
            sb.append(s);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    public static String arrayToStringReverse(String ar[], String del) {
        StringBuilder sb = new StringBuilder();
        for (int i = ar.length - 1; i >= 0; i--) {
            sb.append(ar[i]);
            sb.append(del);
        }
        return sb.toString().trim();
    }
}
