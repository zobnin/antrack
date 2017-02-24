package org.antrack.app;

import android.app.Activity;

import org.antrack.app.libs.L;
import org.antrack.app.libs.Net;
import org.antrack.app.plugins.Dropbox;

import java.io.File;
import java.io.InterruptedIOException;
import java.util.ArrayList;

// Pw - Plugin wrapper

public class Pw {
    private static volatile Pw instance;
    public static Pw getInstance() {
        Pw localInstance = instance;
        if (localInstance == null) {
            synchronized (Pw.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new Pw();
                }
            }
        }
        return localInstance;
    }

    private static final String TAG = "Pw";

    // Max sleep time if no connection to cloud
    private static final int MAX_SLEEP = 320;

    private Settings settings;
    private Dropbox dPlugin;

    private boolean connected = false;

    private Pw() {
        connect();
    }

    // на самом деле объект dPlugin будет успешно создан и без интернета
    // но проверка на коннект позволяет избежать всяких ситуаций с NullException
    public boolean connect() {
        if (connected) {
            return true;
        }

        settings = Settings.getInstance();

        String token = settings.readToken();
        if (token == null || token.equals("")) {
            return false;
        }

        if ("dropbox".equals(settings.get(C.S_PLUGIN))) {
            dPlugin = new Dropbox(token);
            connected = true;
            L.d(TAG, "Connected to cloud");
            return true;
        }

        return false;
    }

    public boolean isConnected() {
        if (connected) {
            return true;
        } else {
            L.e(TAG, "No connection to cloud");
            return false;
        }
    }

    public void auth(Activity activity) throws InterruptedException {
        if (settings.get("plugin").equals("dropbox")) {
            dPlugin = new Dropbox();
            dPlugin.auth(activity);
        }
    }

    public String resume() {
        if (settings.get("plugin").equals("dropbox")) {
            return dPlugin.resume();
        } else {
            return null;
        }
    }

    public String getEmail() {
        if (!connect()) return null;

        if (settings.get("plugin").equals("dropbox")) {
            return dPlugin.getEmail();
        } else {
            return null;
        }
    }

    public void putFile(String lFile, String rFile, boolean delete) throws InterruptedException {
        if (!connect()) return;

        if (settings.get("plugin").equals("dropbox")) {
            L.d(TAG, "Put file " + lFile + " as " + rFile);
            dPlugin.putFile(lFile, rFile, delete);
        }
    }

    public void getFile(String lFile, String rFile) throws InterruptedException {
        if (!connect()) return;

        if (settings.get("plugin").equals("dropbox")) {
            L.d(TAG, "Get file " + rFile + " as " + lFile);
            dPlugin.getFile(lFile, rFile);
        }
    }

    public void delete(String rFile, boolean permanent) throws InterruptedException {
        if (!connect()) return;

        if (settings.get("plugin").equals("dropbox")) {
            dPlugin.delete(rFile, permanent);
        }
    }

    public ArrayList<String> listDir(String rDir) throws InterruptedException  {
        if (!connect()) return null;

        if (settings.get("plugin").equals("dropbox")) {
            L.d(TAG, rDir);
            return dPlugin.listDir(rDir);
        }
        return null;
    }

    public ArrayList<String> listDir(String rDir, boolean withDeleted) throws InterruptedException  {
        if (!connect()) return null;

        if (settings.get("plugin").equals("dropbox")) {
            L.d(TAG, rDir);
            return dPlugin.listDir(rDir, withDeleted);
        }
        return null;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void getDir(String lDir, String rDir) throws InterruptedException {
        if (!connect()) return;

        if (settings.get("plugin").equals("dropbox")) {
            L.d(TAG, "Get files from dir " + rDir + " to dir " + lDir);

            // We don't want to trigger fileObserver on every downloaded file
            // so we save dir to main folder and then move to devices folder
            String tempDir = Init.getInstance().APP_DIR + "/" + new File(rDir).getName();
            new File(tempDir).mkdirs();
            dPlugin.getDir(tempDir, rDir);
            new File(tempDir).renameTo(new File(lDir));
        }
    }

    public ArrayList<String> listDirs(String rDir) throws InterruptedIOException  {
        if (!connect()) return null;

        if (settings.get("plugin").equals("dropbox")) {
            L.d(TAG, "List dirs " + rDir);
            return dPlugin.listDirs(rDir);
        }
        return null;
    }

    public ArrayList<String> watchForChanges(String dir) throws InterruptedException {
        if (!connect()) return null;

        if (settings.get("plugin").equals("dropbox")) {
            L.d(TAG, "Start watching");
            return dPlugin.watchForChanges(dir);
        }
        return null;
    }

    public synchronized void waitOnline() throws InterruptedException {
        int i = 10;
        while (!Net.isOnline()) {
            L.d(TAG, "No internet, sleep " + i + " seconds");
            Thread.sleep(i * 1000);
            if (i < MAX_SLEEP)
                i = i * 2;
        }
    }
}
