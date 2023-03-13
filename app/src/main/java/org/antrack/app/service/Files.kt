package org.antrack.app.service

import org.antrack.app.ACCURATE_TIME_FORMAT
import org.antrack.app.App
import org.antrack.app.BOOTSTRAP_ASSET
import org.antrack.app.Env
import org.antrack.app.functions.addLine
import org.antrack.app.functions.formatDate
import org.antrack.app.functions.readAsList
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*

object Files {
    fun readBootstrap(): List<String> {
        val iStream = App.context.assets.open(BOOTSTRAP_ASSET)

        BufferedReader(InputStreamReader(iStream)).use { reader ->
            return reader.lineSequence().toList()
        }
    }

    fun readCtlFile(): String {
        return File(Env.ctlFilePath).readText()
    }

    fun readCtlqFile(): List<String> {
        return File(Env.ctlqFilePath).readAsList()
    }

    fun writeCmdResult(cmd: String, result: String) {
        File(Env.resultFilePath).apply {
            val date = formatDate(Date().time, ACCURATE_TIME_FORMAT)
            writeText("$date\n$cmd $result\n")
        }
    }

    fun writeErrorResult(result: String) {
        writeCmdResult("internal", "error: $result")
    }

    fun writeTestCmdResult(cmd: String, result: String) {
        File(Env.testingFilePath).addLine("$cmd $result\n")
    }

    fun removeTestResultFile() {
        File(Env.testingFilePath).delete()
    }
}