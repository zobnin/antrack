package org.antrack.app.ui;

import org.antrack.app.C;
import org.antrack.app.Init;
import org.antrack.app.libs.Crypto;
import org.antrack.app.libs.Files;
import org.antrack.app.libs.L;

import java.io.IOException;
import java.security.PublicKey;

public class Device {
    private final String TAG = "Device";

    private String dirName;
    private String OSId = null;
    private PublicKey key;

    public String lastUpdate = null;

    Device(String dir) {
        dirName = dir;
    }

    public boolean isMain() {
        return dirName.equals(Init.getInstance().DEVICE_NAME_IMEI);
    }

    public String getName() {
        return dirName.substring(0, dirName.lastIndexOf('_')).replace('_', ' ');
    }

    public String getDir() {
        return dirName;
    }

    public String getOSId() {
        if (OSId == null) {
            try {
                OSId = Files.readTextFile(Init.getInstance().DEVICES_DIR + dirName + C.OSID_FILE);
            } catch (IOException e ) {
                L.d("Device", "Can't read osid file: " + e.toString());
            }
        }
        return OSId;
    }

    public PublicKey getPublicKey() {
        if (key == null) {
            try {
                String stringKey = Files.readTextFile(U.getLocalPath(C.PUBLIC_KEY_FILE));
                key = Crypto.stringToPublicKey(stringKey.trim());
            } catch (Exception e) {
                L.d(TAG, "Can't read key file: " + e.toString());
            }
        }
        return key;
    }

    public String getFullName() {
        String fullName;
        try {
            fullName = Files.readTextFile(Init.getInstance().DEVICES_DIR + dirName + "/name");
        } catch (IOException e) {
            L.d("Device", "Can't read full name: " + e.toString());
            fullName = null;
        }
        return fullName;
    }
}
