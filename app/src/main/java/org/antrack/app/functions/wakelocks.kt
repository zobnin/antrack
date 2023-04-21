package org.antrack.app.functions

import android.content.Context
import android.os.PowerManager
import org.antrack.app.App

private val wakeLock = (App.context.getSystemService(Context.POWER_SERVICE) as PowerManager)
    .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "antrack:wakelock")

fun wakelock(block: () -> Unit) {
    lock()
    block()
    unlock()
}

private fun lock() {
    try {
        wakeLock.acquire(1 * 60 * 1000L /* 1 minute */)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun unlock() {
    if (wakeLock.isHeld) {
        wakeLock.release()
    }
}
