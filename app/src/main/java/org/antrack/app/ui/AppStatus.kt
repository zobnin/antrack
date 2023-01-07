package org.antrack.app.ui

import android.content.Context
import android.os.Build
import android.os.Environment
import app.BuildConfig
import org.antrack.app.Settings
import org.antrack.app.functions.isIgnoringBatteryOptimizations
import org.antrack.app.libs.Admin
import org.antrack.app.libs.Shell
import org.antrack.app.service.CloudService

class AppStatus(private val context: Context) {
    val version: String
        get() = BuildConfig.VERSION_NAME

    val isServiceEnabled: Boolean
        get() = CloudService.isWorking(context)

    val cloudPlugin: String
        get() = Settings.plugin

    val isIgnoringBatteryOptimizations: Boolean
        get() = context.isIgnoringBatteryOptimizations()

    val haveAccessToAllFiles: Boolean
        get() = when {
            Build.VERSION.SDK_INT >= 30 -> Environment.isExternalStorageManager()
            else -> true
        }

    val haveAdminRights: Boolean
        get() = Admin().isActive

    val haveRootRights: Boolean
        get() = Shell.checkSu()
}
