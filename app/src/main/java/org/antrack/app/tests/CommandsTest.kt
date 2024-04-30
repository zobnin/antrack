package org.antrack.app.tests

import android.content.Context
import org.antrack.app.Env
import org.antrack.app.functions.sleep
import org.antrack.app.functions.sleepS
import org.antrack.app.service.CloudService
import org.antrack.app.service.Files
import org.antrack.app.watcher.CloudWatcher
import org.antrack.app.watcher.FileWatcher
import java.io.File
import java.util.Date

class CommandsTest(private val context: Context) : Test {

    private val commands = listOf(
        "@cmd uname",
        "@cmd xxx",
        "@status; info",
        "@    info   ",
        "@status; @info",
        "@XXX",
        "@",
        "cmd uname", // silent
    )

    private val expected = listOf(
        "cmd done",
        "cmd error: no output",
        "status done",
        "info done",
        "info done",
        "status done",
        "@info error: no such module",
        "XXX error: no such module",
        "internal error: command should be 2..200 symbols",
    )

    override fun before() {
        // Slows down the tests
        FileWatcher.removeCallback("service_uploader")
        // Can affect the results
        CloudWatcher.removeCallback("service_cloud_watcher")
        // Multithreaded execution don't allow to read result on time
        FileWatcher.multithreded = false
    }

    override fun after() {
        FileWatcher.multithreded = true
        CloudService.stop(context)
        CloudService.start(context)
    }

    override fun run(): List<String> {
        return  runCtlTest() +
                runCtlqTest() +
                runCtlqFuzzingTest() +
                runCtlqTimeTest()
    }

    private fun runCtlTest(): List<String> {
        val ctlFile = File(Env.ctlFilePath)

        Files.purgeTestResultFile()

        commands.forEach {
            ctlFile.writeText(it)
            sleep(100)
        }

        val results = Files.readTestResultFile()

        return checkResults(ctlFile, expected, results)
    }

    private fun runCtlqTest(): List<String> {
        val ctlqFile = File(Env.ctlqFilePath)

        Files.purgeTestResultFile()

        var text = ""
        commands.forEach {
            text = text + Date().time + " " + it + "\n"
            sleep(100)
        }

        ctlqFile.writeText(text)
        sleepS(1)

        val results = Files.readTestResultFile()

        return checkResults(ctlqFile, expected, results)
    }

    private fun runCtlqFuzzingTest(): List<String> {
        val commands = listOf(
            "zzzzzzzzzzzz",
            "121213 1313131",
            "dewdew lopkoijoi bhyhyu",
            "X",
            "x".repeat(1_000),
            "${Date().time} @cmd uname"
        )

        val expected = listOf(
            "cmd done"
        )

        return ctlqGenericTest(commands, expected)
    }

    private fun runCtlqTimeTest(): List<String> {
        val time = Date().time

        val commands = listOf(
            "$time @info",
            "${time+1} @status",
            "${time+2} @info",
            "${time-10} @status"
        )

        val expected = listOf(
            "info done",
            "status done",
            "info done"
        )

        return ctlqGenericTest(commands, expected)
    }

    private fun ctlqGenericTest(commands: List<String>, expected: List<String>): List<String> {
        val ctlqFile = File(Env.ctlqFilePath)

        Files.purgeTestResultFile()

        var text = ""
        commands.forEach {
            text = text + it + "\n"
            sleep(100)
        }

        ctlqFile.writeText(text)
        sleepS(1)

        val results = Files.readTestResultFile()

        return checkResults(ctlqFile, expected, results)
    }

    private fun checkResults(
        ctlFile: File,
        expected: List<String>,
        results: List<String>,
    ): List<String> {

        if (results.size != expected.size) {
            return listOf(
                "${ctlFile.name} test passed: false",
                "Expected:\n$expected",
                "Actual:\n$results",
            )
        }

        val info = results
            .mapIndexed { idx, result -> checkResult(expected.getOrNull(idx), result) }
            .filterNotNull()

        return info.ifEmpty { listOf("${ctlFile.name} test passed: true") }
    }

    private fun checkResult(exp: String?, actual: String?) = when {
        actual != exp -> "Expected: $exp\nActual: $actual"
        else -> null
    }
}