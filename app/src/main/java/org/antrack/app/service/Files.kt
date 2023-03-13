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

    // Testing only

    fun writeTestCmdResult(cmd: String, result: String) {
        File(Env.testingFilePath).apply {
            addLine("$cmd $result")
        }
    }

    fun writeTestErrorResult(result: String) {
        writeTestCmdResult("internal", "error: $result")
    }

    fun purgeTestResultFile() {
        File(Env.testingFilePath).delete()
        File(Env.testingFilePath).createNewFile()
    }

    fun readTestResultFile(): List<String> {
        return File(Env.testingFilePath).readAsList()
    }
}