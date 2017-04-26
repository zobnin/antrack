package org.antrack.app.libs;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Shell {
    static private String TAG="Shell";

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
        String listGovsFile = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
        return runCommand("cat " + listGovsFile, false, true).split(" ");
    }

    static public boolean changeGov(String gov) {
        String chGovFile = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
        runCommand("echo " + gov + " > " + chGovFile, true, false);
        String newgov = runCommand("cat " + chGovFile, false, true);
        return newgov != null && newgov.equals(gov);
    }

    static public boolean itsQualcomm() {
        String cpuinfo = runCommand("cat /proc/cpuinfo", false, true);
        return cpuinfo != null && (cpuinfo.contains("Qualcomm"));
    }

    static public boolean checkSuRun() {
        String uid = runCommand("id", true, true);
        return uid != null && uid.startsWith("uid=0");
    }

    static public boolean checkSu() {
        String[] paths = { "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su",
                "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su",
                "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/su",
                "/su/bin/su"};
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
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

        } catch (Exception e) {
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
            StringBuilder output = new StringBuilder();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();

            process.waitFor();
            return output.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
