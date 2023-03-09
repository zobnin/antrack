package org.antrack.app.functions

import android.util.Log
import org.antrack.app.*
import java.io.*

import java.util.*

fun logD(tag: String, msg: String) {
    if (LOG_TO_LOGCAT) Log.d("AnTrack", "$tag: $msg")
    if (LOG_TO_FILE) addLineToFile(msg)
}

fun logE(tag: String, msg: String) {
    if (LOG_TO_LOGCAT) Log.e(tag, msg)
    if (LOG_TO_FILE) addLineToFile("[E] $msg")
}

private fun addLineToFile(line: String) {
    try {
        var logFile = newFile(Env.logFilePath)

        if (logFile.length() > LOGS_MAX_LENGTH) {
            logFile.renameTo(File(Env.logFilePath + ".old"))
            logFile = newFile(Env.logFilePath)
        }

        logFile.addLine(formatDate(Date().time, LOG_TIME_FORMAT) + " " + line)
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

private fun newFile(path: String) = File(path).apply { touch() }

private fun File.addLine(text: String) {
    PrintWriter(BufferedWriter(FileWriter(this, true))).use {
        it.println(text)
    }
}

