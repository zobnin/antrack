package org.antrack.app.service;

import android.content.Context;
import android.util.Log;

import org.antrack.app.C;
import org.antrack.app.Init;
import org.antrack.app.Settings;
import org.antrack.app.libs.Files;
import org.antrack.app.libs.L;
import org.antrack.app.libs.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

class CC {
    private static final String TAG = "CC";

    private Context context;
    private Modules modules;

    private boolean internalCommand = false;

    CC(Context context) {
        this.context = context;
        modules = new Modules(context);
    }

    void runModules(String action, String extra) {
        modules.run(action, extra);
    }

    void parseBootstrap() {
        try {
            InputStream is = context.getAssets().open(C.BOOTSTRAP_ASSET);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() > 2) {
                    L.d(TAG, "get bootstrap line: " + line);
                    parseCommand(line.trim());
                }
            }
            reader.close();
        } catch (IOException e) {
            L.e(TAG, "unpackBootstrap error: " + e.toString());
        }
    }

    void parseCommand(String cmd) {
        L.d(TAG, "command: " + cmd);

        if (cmd.length() > 200) {
            writeResult("internal", "error: command can't be > 200 symbols");
            return;
        }

        // Commands that start with "!" are internal: no write /result, no logged
        if (cmd.startsWith("!")) {
            cmd = cmd.substring(1);
            internalCommand = true;
        } else {
            internalCommand = false;
        }

        if (!internalCommand) {
            Logger.getCommand(context, cmd);
        }

        // Parse multi-command
        String[] cmds;
        if (cmd.contains(";")) {
            cmds = cmd.split(";");
        } else {
            cmds = new String[] { cmd };
        }

        // Parse command
        for (String oneCmd : cmds) {
            oneCmd = oneCmd.trim();
            String[] cmdAndArgs = oneCmd.split(" ");

            switch (cmdAndArgs[0]) {
                // Activate "lost" mode
                case "lost":
                    writeResult(cmdAndArgs[0], lost(cmdAndArgs[1]));
                    break;
                // Dump /modules
                case "modules":
                    writeResult(cmdAndArgs[0], modules.listModules());
                    break;
                // Dump /modules.json
                case "dumpjson":
                    writeResult(cmdAndArgs[0], modules.dumpJSON());
                    break;
                // Run module onCommand
                default:
                    String result = modules.command(cmdAndArgs[0], Arrays.copyOfRange(cmdAndArgs, 1, cmdAndArgs.length));
                    writeResult(cmdAndArgs[0], result);
            }
        }
    }

    private void writeResult(String cmd, String result) {
        if (result != null && !internalCommand) {
            try {
                Files.writeTextFile(Init.RESULT_FILE, Utils.date("yyyy.MM.dd HH:mm:ss:SSS"));
                Files.addLine(Init.RESULT_FILE, cmd + " " + result);
            } catch (IOException e) {
                L.e(TAG, "writeResult IOException: " + e);
            }
        }
    }

    private String lost(String pin) {
        /*
         * 1. Lock screen
         * 2. Hide app
         * 3. Turn on setting "photo on screen on"
         * 4. Turn on setting "send SMS after sim change"
         */

        if (pin.equals(C.OFF)) {
            //hide("off");

            Settings.put(C.S_SCREEN_ON_PHOTO, C.FALSE);
            Settings.put(C.S_SMS_ON_SIM_CHANGE, C.FALSE);

            Logger.unlost(context);
        } else {
            //String ret = lock(pin);
            //if (!ret.equals("done")) { return ret; }

            //hide("on");

            Settings.put(C.S_SCREEN_ON_PHOTO, C.TRUE);
            Settings.put(C.S_SMS_ON_SIM_CHANGE, C.TRUE);

            Logger.lost(context);
        }

        return C.DONE;
    }
}
