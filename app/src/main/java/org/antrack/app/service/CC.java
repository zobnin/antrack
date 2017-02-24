package org.antrack.app.service;

import android.content.Context;

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
    private Settings settings;

    private boolean internalCommand = false;

    CC(Context context) {
        this.context = context;
        settings = Settings.getInstance();
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

        if (!internalCommand) {
            Logger.runCommand(context, cmd);
        }

    }

    private void writeResult(String cmd, String result) {
        if (result != null && !internalCommand) {
            try {
                Init init = Init.getInstance();
                Files.writeTextFile(init.RESULT_FILE, Utils.date(C.ACCURATE_TIME_FORMAT));
                Files.addLine(init.RESULT_FILE, cmd + " " + result);
            } catch (IOException e) {
                L.e(TAG, "writeResult IOException: " + e);
            }
        }
    }

    private String lost(String pin) {
        /*
         * 1. Hide app
         * 2. Enable password
         * 3. Remove other devices files
         * 4. Turn on setting "photo on screen on"
         */

        if (pin.equals(C.ON)) {
            parseCommand("hide on");

            settings.put(C.S_SCREEN_ON_PHOTO, C.TRUE);

            Logger.lost(context);
        } else {
            parseCommand("hide off");

            settings.put(C.S_SCREEN_ON_PHOTO, C.FALSE);

            Logger.unlost(context);
        }

        return C.DONE;
    }
}
