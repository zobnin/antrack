package org.antrack.app.service

import org.antrack.app.C
import org.antrack.app.Init
import org.antrack.app.libs.Files
import org.antrack.app.libs.L
import org.antrack.app.libs.Shell
import org.antrack.app.libs.Utils

import java.io.File
import java.io.IOException

object Logger {
    private val TAG = "Logger"

    private fun put(txt: String) {
        val logfile = Init.MAIN_DIR + C.MAIN_LOG_FILE

        try {
            L.d(TAG, "New record: " + txt)

            val file = File(logfile)
            if (file.exists()) {
                val ln = Files.countLines(logfile)
                if (ln > C.LOGS_MAX) {
                    Shell.runCommand("mv $logfile $logfile.old")
                }
            }
            Files.addLine(logfile, Utils.date(C.DEFAULT_TIME_FORMAT) + " " + txt)
        } catch (e: IOException) {
            L.e(TAG, "put IOException")
        }

    }

    fun runCommand(cmd: String) {
        put("[info] Exec command: " + cmd)
    }

    fun getPush(device: String, cmd: String) {
        put("[info] Get command: $cmd, from device: $device")
    }

    fun booted() {
        put("[info] Phone booted")
    }

    fun shutdown() {
        put("[info] Phone power off")
    }

    fun connected() {
        put("[info] Connected to network")
    }

    fun disconnected() {
        put("[info] Disconnected from network")
    }

    fun started() {
        put("[info] Service started")
    }

    fun stopped() {
        put("[warning] Service stopped")
    }

    fun hided() {
        put("[warning] App hided")
    }

    fun unhided() {
        put("[info] App unhided")
    }

    fun lost() {
        put("[warning] Phone marked as lost")
    }

    fun unlost() {
        put("[info] Phone market as not lost")
    }

    fun simChanged() {
        put("[warning] SIM Change detected!")
    }

    fun alarm() {
        put("[info] Periodic tasks launched")
    }

    fun cantDecrypt(deviceName: String) {
        put("[warning] Can't decrypt message from " + deviceName)
    }

    fun trusted(deviceName: String) {
        put("[info] Device $deviceName now trusted")
    }

    fun banned(deviceName: String) {
        put("[info] Device $deviceName now banned")
    }
}
