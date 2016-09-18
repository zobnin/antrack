package org.antrack.app.ui;

import android.util.Log;

import org.antrack.app.Init;
import org.antrack.app.libs.Files;

import java.io.IOException;

public class Device {
    String dirName;

    public Device(String dir) {
        dirName = dir;
    }

    public boolean isMain() {
        return dirName.equals(Init.DEVICE_NAME_IMEI);
    }

    public String getName() {
        return dirName.substring(0, dirName.indexOf('_'));
    }

    public String getDir() {
        return dirName;
    }

    public String getFullName() {
        String fullName;
        try {
            fullName = Files.readTextFile(Init.DEVICES_DIR + dirName + "/name");
        } catch (IOException e) {
            Log.d("Device", "Can't read full name: " + e.toString());
            fullName = null;
        }
        return fullName;
    }
}
