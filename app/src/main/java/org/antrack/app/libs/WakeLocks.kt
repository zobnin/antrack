package org.antrack.app.libs

import android.content.Context
import android.os.PowerManager

class WakeLocks(context: Context, name: String) {
    private val wakeLock: PowerManager.WakeLock = (context
            .getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK, name)

    private val TAG = "WakeLocks"

    fun lock() {
        try {
            wakeLock.acquire()
            L.d(TAG, "Lock acquired")
        } catch (e: Exception) {
            L.e(TAG, "Error getting Lock: " + e.message)
        }

    }

    fun unlock() {
        if (wakeLock.isHeld)
            wakeLock.release()
        L.d(TAG, "Lock released")
    }
}
