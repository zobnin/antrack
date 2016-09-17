package org.antrack.app.ui;

import android.util.Log;

import org.antrack.app.C;
import org.antrack.app.Init;
import org.antrack.app.Pw;
import org.antrack.app.libs.Files;
import org.antrack.app.libs.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class U {
    final static String TAG = "U";

    public static boolean isDeviceMain() {
        return V.currentDevice.equals(Init.DEVICE_NAME_IMEI);
    }

    public static String getDisplayName(String device) {
        int index = device.indexOf('_');
        if (index < 0)
            return device;
        else
            return device.substring(0, device.indexOf('_'));
    }

    // Download file for current device
    public static void getFile(String file) {
        try {
            Files.mkdirsForFile(Init.DEVICES_DIR + V.currentDevice + file);

            Pw pw = Pw.getInstance();
            if (pw.isConnected())
                pw.getFile(Init.DEVICES_DIR + V.currentDevice + file, "/" + V.currentDevice + file);
            // FIXME else throw...
        } catch (Exception e) {
            Log.e(TAG, "Can't get file " + file + ":" + e);
        }
    }

    public static void getFileAsync(final String file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getFile(file);
            }
        }).start();
    }

    // Download all files in dir for current device
    public static void getDir(String dir) {
        try {
            Pw pw = Pw.getInstance();
            if (pw.isConnected())
                pw.getDir(Init.DEVICES_DIR + V.currentDevice + dir, "/" + V.currentDevice + dir);
            // FIXME else throw...
        } catch (Exception e) {
            Log.e(TAG, "Can't get dir " + dir + ": " + e);
        }
    }

    public static void getDirAsync(final String dir) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getDir(dir);
            }
        }).start();

    }

    // Upload file for current device
    public static void putFile(String file) {
        try {
            Pw pw = Pw.getInstance();
            if (pw.isConnected())
                pw.putFile(Init.DEVICES_DIR + V.currentDevice + file, "/" + V.currentDevice + file, false);
            // FIXME else throw...
        } catch (Exception e) {
            Log.e(TAG, "Can't put file " + file + ": " + e);
        }
    }

    public static void putFileAsync(final String file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                putFile(file);
            }
        }).start();
    }

    private volatile static ArrayList<String> listDirResult;

    // List dir (arg: full path, return: full path)
    public static ArrayList<String> listDir(final String dir) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Pw pw = Pw.getInstance();
                    if (pw.isConnected())
                        listDirResult = pw.listDir(dir);
                    // FIXME else throw...
                } catch (Exception e) {
                    Log.e(TAG, "Can't list dir " + dir + ": " + e);
                }
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Log.e(TAG, "listDir exception: " + e);
            return null;
        }
        return listDirResult;
    }

    // Compare cloud and local dir for current device
    public static ArrayList<String> compareDirs(String dir) {
        ArrayList<String> remoteDir, localDir, notInlocal;

        remoteDir = listDir("/" + V.currentDevice + dir);
        localDir = new ArrayList<>(Arrays.asList(new File(getFullPath(dir)).list()));
        notInlocal = new ArrayList<>();

        for (String path : remoteDir) {
            String file = new File(path).getName();
            if (!localDir.contains(file)) {
                notInlocal.add(file);
            }
        }

        return notInlocal;
    }

    public static void runCommandAsync(String cmd) {
        if (isDeviceMain()) {
            cmd = "!" + cmd;

            String deviceControlFile = getFullPath(C.CONTROL_FILE);

            try {
                Files.writeTextFile(deviceControlFile, cmd);
            } catch (IOException e) {
                Log.e(TAG, "Can't run command " + cmd + ": " + e);
            }
        }
        else {
            String deviceControlFile = getFullPath(C.CONTROL_Q_FILE);

            try {
                Files.addLineToStack(deviceControlFile, Utils.date(C.LAST_CMD_TIME_FORMAT) + " " + cmd, C.CONTROL_Q_MAX_LENGHT);
                putFileAsync(C.CONTROL_Q_FILE);
            } catch (IOException e) {
                Log.e(TAG, "Can't run command " + cmd + ": " + e);
            }
        }
    }

    // Get full dir name with main dir and current device name
    public static String getFullPath(String path) {
        return Init.DEVICES_DIR + V.currentDevice + path;
    }

    // Read modules and save in V.modules
    static public boolean initModules() {
        LinkedHashMap<String, Module> modules = new LinkedHashMap<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(getFullPath(C.MODULES_FILE)));
            String line;
            Module module = new Module();
            while ((line = reader.readLine()) != null) {
                String[] pair = line.split(":");
                switch (pair[0]) {
                    case "Name":
                        module.name = pair[1].trim();
                        break;
                    case "Version":
                        module.version = pair[1].trim();
                        break;
                    case "Author":
                        module.author = pair[1].trim();
                        break;
                    case "Description":
                        module.desc = pair[1].trim();
                        break;
                    case "Command":
                        module.command = pair[1].trim();
                        break;
                    case "Uses root":
                        module.usesRoot = pair[1].trim();
                        break;
                    case "Uses admin":
                        module.usesAdmin = pair[1].trim();
                        break;
                    case "Result file":
                        String result = pair[1].trim();
                        module.result = result;
                        // Make dirs for module
                        if (result.endsWith("/")) {
                            //noinspection ResultOfMethodCallIgnored
                            new File(Init.DEVICES_DIR + V.currentDevice + result).mkdir();
                        }
                        break;
                    case "Start when":
                        module.startWhen = pair[1].trim();
                        break;
                    default:
                        modules.put(module.name, module);
                        module = new Module();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Can't read modules file: " + e);
            return false;
        }

        V.modules = modules;
        return true;
    }
}
