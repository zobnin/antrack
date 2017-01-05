package org.antrack.app.libs;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.PowerManager;

public class WakeLocks {
    private PowerManager.WakeLock myWakeLock;
    private WifiManager.WifiLock myWifiLock;

    private String TAG = "WakeLocks";

    public WakeLocks(Context context) {
        /*
        myWifiLock = ((WifiManager) context
                .getSystemService(Context.WIFI_SERVICE)).createWifiLock(
                WifiManager.WIFI_MODE_FULL_HIGH_PERF, "GenericWifiLock");
        */
        myWakeLock = ((PowerManager) context
                .getSystemService(Context.POWER_SERVICE)).newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, "GenericWakelock");
        L.d(TAG, "Lock initiated");
    }

    public void lock() {
        try {
            myWakeLock.acquire();
            //myWifiLock.acquire();
            L.d(TAG,"Lock acquired");
        } catch (Exception e) {
            L.e(TAG, "Error getting Lock: " + e.getMessage());
        }
    }

    public void unlock() {
        if (myWakeLock.isHeld())
            myWakeLock.release();
        /*
        if (myWifiLock.isHeld())
            myWifiLock.release();
        */
        L.d(TAG,"Lock released");
    }
}
