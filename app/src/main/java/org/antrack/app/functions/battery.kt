package org.antrack.app.functions

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings

@SuppressLint("BatteryLife")
fun Context.requestIgnoreBatteryOptimisation() {
    val pm = getSystemService(Context.POWER_SERVICE) as PowerManager

    if (!pm.isIgnoringBatteryOptimizations(packageName)) {
        val intent = Intent().apply {
            action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }
}

fun Context.openBatteryOptimizationSettings() {
    val pm = getSystemService(Context.POWER_SERVICE) as PowerManager

    if (!pm.isIgnoringBatteryOptimizations(packageName)) {
        val intent = Intent().apply {
            action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
        }
        startActivity(intent)
    }
}

fun Context.isIgnoringBatteryOptimizations(): Boolean {
    return (getSystemService(Context.POWER_SERVICE) as PowerManager)
        .isIgnoringBatteryOptimizations(packageName)
}
