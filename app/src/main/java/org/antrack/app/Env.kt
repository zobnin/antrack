package org.antrack.app

import android.os.Build
import androidId
import org.antrack.app.functions.touch

import java.io.File

import java.util.*

object Env {
    private val deviceName = Build.MODEL
        .lowercase(Locale.ROOT)
        .replace(" ", "_")

    var testingDir = ""

    // Android ID for this device
    val deviceId = App.context.androidId ?: ""

    // This device actual name in the app
    val deviceNameId = deviceName + "_" + deviceId.takeLast(4)

    // App directory
    val appDirPath = when {
        testingDir.isNotEmpty() -> testingDir
        else -> App.dataDir
    }

    // App directory + devices + main device name
    val mainDirPath = "$appDirPath/$deviceNameId"

    // Directory where modules is located
    val modulesDirPath = appDirPath + MODULES_DIR

    // Full paths to files
    val ctlFilePath = mainDirPath + CONTROL_FILE
    val ctlqFilePath = mainDirPath + CONTROL_Q_FILE
    val resultFilePath = mainDirPath + RESULT_FILE
    val logFilePath = mainDirPath + LOG_FILE
    val modulesFilePath = mainDirPath + MODULES_FILE
    val lostFilePath = mainDirPath + LOST_FILE
    val featuresFilePath = mainDirPath + FEATURES_FILE

    val cloudCtlPath = "/$deviceNameId$CONTROL_FILE"
    val cloudCtlqPath = "/$deviceNameId$CONTROL_Q_FILE"

    init {
        makeDirs()
    }

    private fun makeDirs() {
        File(mainDirPath).mkdirs()
        File(ctlFilePath).touch()
        File(ctlqFilePath).touch()
    }
}
