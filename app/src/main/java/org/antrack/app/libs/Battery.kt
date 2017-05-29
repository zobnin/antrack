package org.antrack.app.libs

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings

object Battery {
    @TargetApi(23)
    fun requestIgnoreBatteryOptimisation(context: Context) {
        val intent = Intent()
        val packageName = context.packageName
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:" + packageName)
            context.startActivity(intent)
        }
    }

    @TargetApi(23)
    fun openBatteryOptimizationSettings(context: Context) {
        val intent = Intent()
        val packageName = context.packageName
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
            context.startActivity(intent)
        }
    }

    @TargetApi(23)
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return (context.getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(context.packageName)

    }
}
