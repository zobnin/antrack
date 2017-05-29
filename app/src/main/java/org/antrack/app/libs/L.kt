package org.antrack.app.libs

import android.os.Environment
import android.util.Log

import java.io.IOException

object L {
    private val LOGCAT = true
    private val FILE = false

    fun d(tag: String, msg: String) {
        if (LOGCAT) Log.d(tag, msg)
        if (FILE) writeToFile("$tag: $msg")
    }

    fun e(tag: String, msg: String) {
        if (LOGCAT) Log.e(tag, msg)
        if (FILE) writeToFile("$tag: $msg")
    }

    private fun writeToFile(msg: String) {
        try {
            Files.addLine(Environment.getDataDirectory().path + "/logs",
                    Utils.date("yyyy.MM.dd HH:mm:ss.SSS") + " " + msg)
        } catch (e: IOException) {
            Log.e("L", "error: " + e)
        }
    }
}
