package org.antrack.app

import android.content.Context
import android.telephony.TelephonyManager

import org.antrack.app.libs.Files
import org.antrack.app.libs.L
import org.antrack.app.libs.Utils

import java.io.IOException

object Init {
    private val TAG = "Init"

    // This device name
    lateinit var DEVICE_NAME: String
    // This device IMEI
    lateinit var DEVICE_IMEI: String
    // This device actual name in app
    lateinit var DEVICE_NAME_IMEI: String

    // App directory
    lateinit var APP_DIR: String
    // App directory + devices
    lateinit var DEVICES_DIR: String
    // App directory + devices + main device name
    lateinit var MAIN_DIR: String

    // Full paths to control and result file
    lateinit var CONTROL_FILE: String
    lateinit var CONTROL_Q_FILE: String
    lateinit var RESULT_FILE: String

    init {
        L.d(TAG, "Initialization...")

        getIMEI(App.context!!)
        makeDirs(App.context!!)
        initSettings(App.context!!)
        initLastCmdTime()
        writeName()
    }

    private fun makeDirs(context: Context) {
        APP_DIR = context.applicationInfo.dataDir
        DEVICES_DIR = APP_DIR + C.DEVICES_DIR

        DEVICE_NAME = android.os.Build.MODEL.toLowerCase()
        DEVICE_NAME = DEVICE_NAME.replace(" ", "_")
        DEVICE_NAME_IMEI = DEVICE_NAME + "_" + DEVICE_IMEI.substring(DEVICE_IMEI.length - 4)

        MAIN_DIR = DEVICES_DIR + DEVICE_NAME_IMEI

        L.d(TAG, "Device dir: " + MAIN_DIR)

        CONTROL_FILE = MAIN_DIR + C.CONTROL_FILE
        CONTROL_Q_FILE = MAIN_DIR + C.CONTROL_Q_FILE
        RESULT_FILE = MAIN_DIR + C.RESULT_FILE

        Files.mkdirs(MAIN_DIR)
        Files.touch(CONTROL_FILE)
        Files.touch(CONTROL_Q_FILE)
    }

    private fun getIMEI(context: Context) {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        DEVICE_IMEI = tm.deviceId
    }

    private fun writeName() {
        try {
            Files.writeTextFile(MAIN_DIR + C.NAME_FILE,
                    android.os.Build.BRAND + " " + android.os.Build.MODEL)
        } catch (e: IOException) {
            L.e(TAG, "Can't write /name: " + e.toString())
        }

    }

    private fun initSettings(context: Context) {
        if (Settings[C.S_UPDATE_INTERVAL].isNullOrEmpty()) {
            Settings.put(C.S_UPDATE_INTERVAL, C.UPDATE_INTERVAL)
        }

        if (Settings[C.S_IMSI].isNullOrEmpty()) {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val IMSI = tm.subscriberId
            if (IMSI != null) {
                Settings.put(C.S_IMSI, IMSI)
            } else {
                L.e(TAG, "Can't get IMSI")
            }
        }
    }

    private fun initLastCmdTime() {
        if (Settings[C.S_LAST_CMD_TIME].isNullOrEmpty()) {
            val currentTime = Utils.date(C.LAST_CMD_TIME_FORMAT)
            Settings.put(C.S_LAST_CMD_TIME, currentTime)
        }
    }

}
