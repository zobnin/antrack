package org.antrack.app.libs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Shell {
    static private String TAG="ShellTools";

    static String listGovsFile = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
    static String chGovFile = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";

    static public String getWifiPassword() {
        String wpa_supplicant = Shell.runCommand("cat /data/misc/wifi/wpa_supplicant.conf", true, true);
        if (wpa_supplicant == null || wpa_supplicant.equals("")) {
            return null;
        }

        Pattern pattern = Pattern.compile("psk=\"(.*?)\"");
        Matcher matcher = pattern.matcher(wpa_supplicant);
        if (matcher.find()) {
            return matcher.group(0).replace("psk=", "");
        }

        return null;
    }

    static public String[] getGovs() {
        return runCommand("cat " + listGovsFile, false, true).split(" ");
    }

    static public boolean changeGov(String gov) {
        runCommand("echo " + gov + " > " + chGovFile, true, false);
        String newgov = runCommand("cat " + chGovFile, false, true);
        return newgov.equals(gov);
    }

    static public boolean itsQualcomm() {
        String cpuinfo = runCommand("cat /proc/cpuinfo", false, true);
        if (cpuinfo == null)
            return false;
        return (cpuinfo.contains("Qualcomm"));
    }

    static public boolean checkSuRun() {
        String uid = runCommand("id", true, true);
        if (uid == null)
            return false;
        return uid.startsWith("uid=0");
    }

    static public boolean checkSu() {
        String[] places = {"/system/bin/", "/system/xbin/"};
        for (String where : places) {
            if (new File(where + "su").exists()) {
                return true;
            }
        }
        // New SuperSU support
        return new File("/su/bin").exists();
    }

    static public boolean remountSystemRW() {
        return runCommandWait("mount -o remount,rw /system", true);
    }

    static public boolean remountSystemRO() {
        return runCommandWait("mount -o remount,ro /system", true);
    }

    static public boolean runCommandWait(String cmd, boolean needsu) {
        try {
            String su = "sh";
            if (needsu) { su = "su"; }

            Process process = Runtime.getRuntime().exec(new String[]{su, "-c", cmd});
            int result = process.waitFor();

            return (result == 0);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static public void runCommand(String cmd) {
        runCommand(cmd, false, false);
    }

    static public void runCommand(String cmd, boolean needsu) {
        runCommand(cmd, needsu, false);
    }

    static public String runCommand(String cmd, boolean needsu, boolean needout) {
        try {
            String su = "sh";
            if (needsu) { su = "su"; }

            Process process = Runtime.getRuntime().exec(new String[]{su, "-c", cmd});

            if (!needout) { return null; }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();

            process.waitFor();
            return output.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
