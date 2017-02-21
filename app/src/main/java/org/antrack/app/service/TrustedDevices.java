package org.antrack.app.service;

import android.content.Context;

import org.antrack.app.AntrackApplication;
import org.antrack.app.C;
import org.antrack.app.Init;
import org.antrack.app.Pw;
import org.antrack.app.libs.Files;
import org.antrack.app.libs.L;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

class TrustedDevices {
    private static volatile TrustedDevices instance;
    public static TrustedDevices getInstance() {
        TrustedDevices localInstance = instance;
        if (localInstance == null) {
            synchronized (Init.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new TrustedDevices();
                }
            }
        }
        return localInstance;
    }

    private static final String TAG = "TrustedDevices";

    private Properties prop;
    private String trustedFile;

    private TrustedDevices() {
        Context context = AntrackApplication.getAppContext();

        trustedFile = context.getApplicationInfo().dataDir + C.TRUSTED_DEVICES_FILE;
        Files.touch(trustedFile);

        try {
            prop = new Properties();
            prop.load(new FileInputStream(trustedFile));
        } catch (Exception e) {
            L.e(TAG, e.toString());
        }
    }

    void ban(String deviceName) {
        putKey(deviceName, "banned");
    }

    boolean trust(String deviceName) {
        Init init = Init.getInstance();

        if (!new File(init.DEVICES_DIR + deviceName + C.PUBLIC_KEY_FILE).exists()) {
            Files.mkdir(init.DEVICES_DIR + deviceName);
            Pw pw = Pw.getInstance();

            if (pw.isConnected()) {
                try {
                    pw.getFile(init.DEVICES_DIR + deviceName + C.PUBLIC_KEY_FILE,
                            "/" + deviceName + C.PUBLIC_KEY_FILE);
                } catch (Exception e) {
                    L.e(TAG, "Can't download public key: " + e.toString());
                    return false;
                }
            } else {
                L.e(TAG, "Can't download public key: not connected");
                return false;
            }
        }

        try {
            String stringKey = Files.readTextFile(
                    init.DEVICES_DIR + deviceName + C.PUBLIC_KEY_FILE);
            putKey(deviceName, stringKey);
        } catch (Exception e) {
            L.e(TAG, "Can't read public key: " + e.toString());
            return false;
        }

        // FIXME log
        return true;
    }

    private void putKey(String name, String value) {
        prop.setProperty(name, value);
        store();
        L.d(TAG, "Add trusted device: " + name + " = " + value);
    }

    String getKey(String name) {
        String value = prop.getProperty(name);
        L.d(TAG, "Get trusted device: " + name + " = " + value);
        return value;
    }

    private void store() {
        try {
            prop.store(new FileOutputStream(trustedFile), "");
        } catch (Exception e) {
            L.e(TAG, e.toString());
        }
    }
}
