package org.antrack.app.service;

import android.util.Log;

import org.antrack.app.C;
import org.antrack.app.Init;
import org.antrack.app.Pw;
import org.antrack.app.Settings;
import org.antrack.app.libs.Files;
import org.antrack.app.libs.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class U {
    // Get full local dir path
    public static String getFullPath(String path) {
        return Init.MAIN_DIR + path;
    }

    // Get full cloud dir path
    public static String getCloudPath(String path) {
        return "/" + Init.DEVICE_NAME + path;
    }

    public static void parseCtl() throws IOException {
        String command = Files.readTextFile(getFullPath(C.CONTROL_FILE)).trim();
        V.cc.parseCommand(command);
    }

    public static void parseCtlq() throws IOException {
        ArrayList<String> cmds = Files.textFileToArray(getFullPath(C.CONTROL_Q_FILE));

        long lastCmdTime = Long.parseLong(Settings.get(C.S_LAST_CMD_TIME));

        String cmdTime = null;
        String cmdName = null;
        for (String cmd : cmds) {
            String[] cmdA = cmd.split(" ");
            cmdTime = cmdA[0].trim();
            cmdName = Utils.arrayToString(Arrays.copyOfRange(cmdA, 1, cmdA.length));

            if (Long.parseLong(cmdTime) > lastCmdTime) {
                V.cc.parseCommand(cmdName);
            }
        }

        if (cmdTime != null) {
            Settings.put(C.S_LAST_CMD_TIME, cmdTime);
        }
    }

    public static void getFile(String file) throws InterruptedException {
        Pw.getFile(U.getFullPath(file), U.getCloudPath(file));
    }

    public static void putFile(String file) throws InterruptedException {
        Pw.putFile(U.getFullPath(file), U.getCloudPath(file), false);
    }

    // Upload file with full path
    public static void uploadFile(String path) throws InterruptedException {
        File file = new File(path);

        if (file.isDirectory()) {
            return;
        }

        if (!file.exists()) {
            Log.d("U/uploadFile", "File don't exist: " + path);
            return;
        }

        // Audio and images must be deleted after upload to save space
        // FIXME расширения вынести в массив в C.java
        //boolean delete = false;
        //if (path.endsWith(".jpg") || path.endsWith(".png") || path.endsWith(".3gp")) {
        //    delete = true;
        //}

        Pw.putFile(path, "/" + path.replace(Init.DEVICES_DIR, ""), false);
    }
}
