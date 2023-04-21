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
    private val ctlFile = File(Env.ctlFilePath)
    private val ctlqFile = File(Env.ctlqFilePath)
    private val resultFile = File(Env.resultFilePath)
    private val testingFile = File(Env.testingFilePath)

    fun readBootstrap(): List<String> {
        val iStream = App.context.assets.open(BOOTSTRAP_ASSET)

        BufferedReader(InputStreamReader(iStream)).use { reader ->
            return reader.lineSequence().toList()
        }
    }

    fun readCtlFile(): String {
        return ctlFile.readText()
    }

    fun readCtlqFile(): List<String> {
        return ctlqFile.readAsList()
    }

    fun writeCmdResult(cmd: String, result: String) {
        resultFile.apply {
            val date = formatDate(Date().time, ACCURATE_TIME_FORMAT)
            writeText("$date\n$cmd $result\n")
        }
    }

    fun writeErrorResult(result: String) {
        writeCmdResult("internal", "error: $result")
    }

    // Testing only

    fun writeTestCmdResult(cmd: String, result: String) {
        testingFile.apply {
            addLine("$cmd $result")
        }
    }

    fun writeTestErrorResult(result: String) {
        writeTestCmdResult("internal", "error: $result")
    }

    fun purgeTestResultFile() {
        testingFile.writeText("")
    }

    fun readTestResultFile(): List<String> {
        return testingFile.readAsList()
    }
}