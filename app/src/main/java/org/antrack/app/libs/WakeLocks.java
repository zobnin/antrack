package org.antrack.app.libs;

import android.content.Context;
import android.os.PowerManager;

public class WakeLocks {
    private PowerManager.WakeLock wakeLock;

    private String TAG = "WakeLocks";

    public WakeLocks(Context context, String name) {
        wakeLock = ((PowerManager) context
                .getSystemService(Context.POWER_SERVICE)).newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, name);
        L.d(TAG, "Lock initiated");
    }

    public void lock() {
        try {
            wakeLock.acquire();
            L.d(TAG,"Lock acquired");
        } catch (Exception e) {
            L.e(TAG, "Error getting Lock: " + e.getMessage());
        }
    }

    public void unlock() {
        if (wakeLock.isHeld())
            wakeLock.release();
        L.d(TAG,"Lock released");
    }
}
