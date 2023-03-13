package org.antrack.app.tests

import org.antrack.app.Env
import org.antrack.app.functions.className
import org.antrack.app.functions.logD
import org.antrack.app.functions.purgeDir
import org.antrack.app.functions.sleep
import org.antrack.app.modules.ModuleInterface
import org.antrack.app.modules.Modules
import java.io.File

abstract class ModuleTest: Test {
    private val ctlFile = File(Env.ctlFilePath)

    fun testModule(
        modName: String,
        command: String = modName,
        block: (String) -> Boolean,
    ): Boolean {
        try {
            logD(className, "Testing $modName module")

            val module = Modules.get()[modName] ?: return block("")

            return when {
                module.result().isEmpty() -> block(noOutTest(module, command))
                module.result().endsWith("/") -> block(dirTest(module, command))
                else -> block(fileTest(module, command))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun testModuleResult(
        moduleName: String,
        command: String,
        expected: String,
    ): Boolean {
        val resultFile = File(Env.resultFilePath)
        resultFile.delete()
        ctlFile.writeText(command)

        while (!resultFile.exists() || resultFile.length() < 1) {
            sleep(100)
        }

        logD(className, "Result: " + resultFile.readText())

        return testResult(moduleName, expected)
    }

    private fun noOutTest(
        module: ModuleInterface,
        command: String
    ): String {
        ctlFile.writeText(command)
        sleep(1000)
        return ""
    }

    private fun dirTest(
        module: ModuleInterface,
        command: String
    ): String {
        val modOutDir = File(Env.mainDirPath + module.result())
        modOutDir.purgeDir()
        val modDirContents = modOutDir.listFiles().toSet()
        ctlFile.writeText(command)

        while (modOutDir.listFiles().toSet() == modDirContents) {
            sleep(100)
        }

        // Give it time to fill the files
        sleep(100)

        val newFiles = (modOutDir.listFiles().toSet() - modDirContents.toSet())
            .joinToString("\n") { it.absolutePath }

        logD(className, "Dir ${modOutDir.name} changed:\n$newFiles")

        return newFiles
    }

    private fun fileTest(
        module: ModuleInterface,
        command: String
    ): String {
        val modOutFile = File(Env.mainDirPath + module.result())
        modOutFile.delete()
        ctlFile.writeText(command)

        while (!modOutFile.exists() || modOutFile.length() < 1) {
            sleep(100)
        }

        logD(className, "File ${modOutFile.name} appeared!")

        return File(Env.mainDirPath + module.result()).readText().trim()
    }

    private fun testResult(moduleName: String, expected: String): Boolean {
        return File(Env.resultFilePath)
            .readText()
            .trim()
            .contains("$moduleName $expected")
    }
}