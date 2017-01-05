package org.antrack.app.ui;

import org.antrack.app.C;
import org.antrack.app.Init;
import org.antrack.app.libs.Files;
import org.antrack.app.libs.L;

import java.io.IOException;

public class Device {
    private String dirName;
    private String OSId = null;
    String lastUpdate = null;

    Device(String dir) {
        dirName = dir;
    }

    public boolean isMain() {
        return dirName.equals(Init.DEVICE_NAME_IMEI);
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
                OSId = Files.readTextFile(Init.DEVICES_DIR + dirName + C.OSID_FILE);
            } catch (IOException e ) {
                L.d("Device", "Can't read osid file: " + e.toString());
            }
        }
        return OSId;
    }

    public String getFullName() {
        String fullName;
        try {
            fullName = Files.readTextFile(Init.DEVICES_DIR + dirName + "/name");
        } catch (IOException e) {
            L.d("Device", "Can't read full name: " + e.toString());
            fullName = null;
        }
        return fullName;
    }
}
