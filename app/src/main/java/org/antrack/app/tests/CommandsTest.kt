package org.antrack.app.tests

import android.content.Context
import org.antrack.app.Env
import org.antrack.app.functions.sleep
import org.antrack.app.service.CloudService
import org.antrack.app.service.Files
import org.antrack.app.watcher.FileWatcher
import java.io.File

class CommandsTest(private val context: Context): Test {
    private val ctlFile = File(Env.ctlFilePath)

    override fun before() {
        // Slows down the tests
        FileWatcher.removeCallback("service_uploader")
        // Multithreaded execution don't allow to read result on time
        FileWatcher.multithreded = false
    }

    override fun after() {
        FileWatcher.multithreded = true
        CloudService.stop(context)
        CloudService.start(context)
    }

    override fun run(): List<String> {
        val commands = listOf(
            "@cmd uname",
            "@cmd xxx",
            "@status; info",
            "@    info   ",
            "@status; @info",
            "@XXX",
            "@",
            "cmd uname", // silent
        )

        val expected = listOf(
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

        Files.purgeTestResultFile()

        commands.forEach {
            ctlFile.writeText(it)
            sleep(100)
        }

        val results = Files.readTestResultFile()

        return checkResults(expected, results)
    }

    private fun checkResults(
        expected: List<String>,
        results: List<String>,
    ): List<String> {

        val info = results
            .mapIndexed { idx, result -> checkResult(expected.getOrNull(idx), result) }
            .filterNotNull()

        return info.ifEmpty { listOf("All test passed: true") }
    }

    private fun checkResult(exp: String?, actual: String?) = when {
        actual != exp -> "Expected: $exp\n\nActual: $actual"
        else -> null
    }
}