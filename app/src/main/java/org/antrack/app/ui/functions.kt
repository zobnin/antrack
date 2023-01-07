package org.antrack.app.ui

import org.antrack.app.Env
import org.antrack.app.functions.logE
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

fun runCommandAsync(cmd: String) {
    try {
        File(Env.ctlFilePath).writeText("!$cmd")
    } catch (e: IOException) {
        logE("runCommandAsync", "Can't run command $cmd: $e")
    }
}

fun readModulesFile(): Map<String, Module> {
    val modulesInt = mutableMapOf<String, Module>()
    val reader = BufferedReader(FileReader(Env.modulesFilePath))
    var module = Module()

    reader.readLines()
        .asSequence()
        .map { line -> line.split(":") }
        .forEach { pair ->
            when (pair[0]) {
                "Name" -> module = module.copy(name = pair[1].trim())
                "Version" -> module = module.copy(version = pair[1].trim())
                "Author" -> module = module.copy(author = pair[1].trim())
                "Description" -> module = module.copy(desc = pair[1].trim())
                "Command" -> module = module.copy(command = pair[1].trim())
                "Uses root" -> module = module.copy(usesRoot = pair[1].trim())
                "Uses admin" -> module = module.copy(usesAdmin = pair[1].trim())
                "Start when" -> module = module.copy(startWhen = pair[1].trim())
                "Result file" -> module = module.copy(result = pair[1].trim())
                else -> {
                    modulesInt[module.name] = module
                    module = Module()
                }
            }
        }

    return modulesInt
}
