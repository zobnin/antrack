package org.antrack.app;

import android.app.Activity;
import android.util.Log;

import org.antrack.app.libs.Net;
import org.antrack.app.plugins.Dropbox;
import org.antrack.app.service.Logger;

import java.io.File;
import java.util.ArrayList;

// Pw - Plugin wrapper

public class Pw {
    private static final String TAG = "Pw";

    static Dropbox dPlugin;

    static String token;
    static boolean connected = false;

    static public boolean isConnected() {
        return connected;
    }

    static public void init() throws InterruptedException {
        isOnline();

        if (connected)
            return;

        Settings.init();

        token = Settings.get(C.S_TOKEN);
        if (Settings.get("plugin").equals("dropbox")) {
            if (token != null) {
                dPlugin = new Dropbox(token);
                connected = true;
                Log.d(TAG, "Connected to dropbox");
            } else {
                Settings.put(C.S_LAUNCH_WIZARD, "true");
                // FIXME просто выйти?
                System.exit(0);
            }
        }
    }

    static public void auth(Activity activity) throws InterruptedException {
        if (Settings.get("plugin").equals("dropbox")) {
            dPlugin = new Dropbox();
            dPlugin.auth(activity);
        }
    }

    static public String resume() {
        if (Settings.get("plugin").equals("dropbox")) {
            return dPlugin.resume();
        } else {
            return null;
        }
    }

    static public void putFile(String lFile, String rFile, boolean delete) throws InterruptedException {
        if (Settings.get("plugin").equals("dropbox")) {
            Log.d(TAG, "Put file " + lFile + " as " + rFile);
            dPlugin.putFile(lFile, rFile, delete);
        }
    }

    static public void getFile(String lFile, String rFile) throws InterruptedException {
        if (Settings.get("plugin").equals("dropbox")) {
            Log.d(TAG, "Get file " + rFile + " as " + lFile);
            dPlugin.getFile(lFile, rFile);
        }
    }

    static public ArrayList<String> listDir(String rDir) throws InterruptedException  {
        if (Settings.get("plugin").equals("dropbox")) {
            Log.d(TAG, rDir);
            return dPlugin.listDir(rDir);
        }
        return null;
    }

    static public void getDir(String lDir, String rDir) throws InterruptedException {
        if (Settings.get("plugin").equals("dropbox")) {
            Log.d(TAG, "Get files from dir " + rDir + " to dir " + lDir);
            // We don't want to trigger fileObserver on every downloaded file
            // so we save dir to main folder and then move to devices folder
            String tempDir = Init.APP_DIR + "/" + new File(rDir).getName();
            new File(tempDir).mkdirs();
            dPlugin.getDir(tempDir, rDir);
            new File(tempDir).renameTo(new File(lDir));
        }
    }

    static public ArrayList<String> listDirs(String rDir) throws InterruptedException  {
        if (Settings.get("plugin").equals("dropbox")) {
            Log.d(TAG, "List dirs " + rDir);
            return dPlugin.listDirs(rDir);
        }
        return null;
    }

    static public ArrayList<String> watchForChanges(String dir) throws InterruptedException {
        isOnline();
        if (Settings.get("plugin").equals("dropbox")) {
            Log.d(TAG, "Start watching");
            return dPlugin.watchForChanges(dir);
        }
        return null;
    }

    static private synchronized void isOnline() throws InterruptedException {
        int i = 1;
        while (!Net.isOnline()) {
            Log.d(TAG, "No internet, sleep " + i + "0 seconds");
            Thread.sleep(i * 10000);
            if (i < 16)
                i = i * 2;
        }
    }
}
