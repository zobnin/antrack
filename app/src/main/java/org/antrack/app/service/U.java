package org.antrack.app.service;

import org.antrack.app.C;
import org.antrack.app.Init;
import org.antrack.app.Pw;
import org.antrack.app.Settings;
import org.antrack.app.libs.Files;
import org.antrack.app.libs.L;
import org.antrack.app.libs.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class U {
    // Get full local dir path
    private static String getFullPath(String path) {
        return Init.MAIN_DIR + path;
    }

    // Get full cloud dir path
    private static String getCloudPath(String path) {
        return "/" + Init.DEVICE_NAME_IMEI + path;
    }

    static void parseCtl(CC cc) throws IOException {
        String command = Files.readTextFile(getFullPath(C.CONTROL_FILE)).trim();
        cc.parseCommand(command);
    }

    static void parseCtlq(CC cc) throws IOException {
        ArrayList<String> cmds = Files.textFileToArray(getFullPath(C.CONTROL_Q_FILE));

        long lastCmdTime = Long.parseLong(Settings.get(C.S_LAST_CMD_TIME));

        for (String cmd : cmds) {
            String[] cmdA = cmd.split(" ");
            String cmdTime = cmdA[0].trim();
            String cmdName = Utils.arrayToString(Arrays.copyOfRange(cmdA, 1, cmdA.length));

            if (Long.parseLong(cmdTime) > lastCmdTime) {
                Settings.put(C.S_LAST_CMD_TIME, cmdTime);
                // This action may crash if module has errors
                cc.parseCommand(cmdName);
            }
        }
    }

    public static void getFile(String file) throws InterruptedException {
        Pw pw = Pw.getInstance();
        if (pw.isConnected())
            pw.getFile(U.getFullPath(file), U.getCloudPath(file));
    }

    public static void putFile(String file) throws InterruptedException {
        Pw pw = Pw.getInstance();
        if (pw.isConnected())
            pw.putFile(U.getFullPath(file), U.getCloudPath(file), false);
    }

    // Upload file with full path
    static void uploadFile(String path) throws InterruptedException {
        Pw pw = Pw.getInstance();
        if (!pw.isConnected())
            return;

        File file = new File(path);

        if (file.isDirectory()) {
            return;
        }

        if (!file.exists()) {
            L.d("U/uploadFile", "File don't exist: " + path);
            return;
        }

        pw.putFile(path, "/" + path.replace(Init.DEVICES_DIR, ""), false);
    }
}
