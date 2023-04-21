package org.antrack.app.modules

import org.antrack.app.DONE
import org.antrack.app.Env
import org.antrack.app.MODULES_JSON_FILE
import org.antrack.app.functions.className
import org.antrack.app.functions.logE
import java.io.BufferedReader
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class ModulesSerializer {
    companion object {
        const val NAME = "Name"
        const val VERSION = "Version"
        const val AUTHOR = "Author"
        const val DESCRIPTION = "Description"
        const val COMMAND = "Command"
        const val USES_ROOT = "Uses root"
        const val USES_ADMIN = "Uses admin"
        const val RESULT_FILE = "Result file"
        const val START_WHEN = "Start when"

        const val NEVER = "never"
        const val NONE = "none"
    }

    val modules: Map<String, ModuleInterface>
        get() = Modules.get()

    fun write(): String {
        if (modules.isEmpty()) return "error: no modules"

        try {
            val writer = FileWriter(Env.modulesFilePath)

            for ((key) in modules) {
                val module = modules[key]
                var info = ""

                info += "$NAME: $key\n"
                info += "$VERSION: ${module!!.version()}\n"
                info += "$AUTHOR: ${module.author()}\n"
                info += "$DESCRIPTION: ${module.desc()}\n"
                info += "$COMMAND: ${module.command()}\n"
                info += "$USES_ROOT: ${module.usesRoot()}\n"
                info += "$USES_ADMIN: ${module.usesAdmin()}\n"

                info += "$RESULT_FILE: "
                info += module.result() ?: NONE
                info += "\n"

                info += "$START_WHEN: "
                info += module.startWhen()?.joinToString(" ") ?: NEVER
                info += "\n"

                info += "\n"
                writer.write(info)
            }
            writer.close()
        } catch (e: IOException) {
            logE(className, "error: ${e.message}")
            e.printStackTrace()
        }

        return DONE
    }

    fun writeJson(): String {
        try {
            FileWriter(Env.mainDirPath + MODULES_JSON_FILE).use { writer ->
                modules.forEach { (name, module) ->
                    val json = genJson(name, module)
                    writer.write(json)
                    writer.flush()
                }
            }
            return DONE
        } catch (e: IOException) {
            e.printStackTrace()
            return "error: $e"
        }
    }

    fun read(): Map<String, Module> {
        val modulesInt = mutableMapOf<String, Module>()
        val reader = BufferedReader(FileReader(Env.modulesFilePath))
        var module = Module()

        reader.readLines()
            .asSequence()
            .map { line -> line.split(":") }
            .forEach { stringArr ->
                when (stringArr[0]) {
                    NAME -> module = module.copy(name = stringArr[1].trim())
                    VERSION -> module = module.copy(version = stringArr[1].trim())
                    AUTHOR -> module = module.copy(author = stringArr[1].trim())
                    DESCRIPTION -> module = module.copy(desc = stringArr[1].trim())
                    COMMAND -> module = module.copy(command = stringArr[1].trim())
                    USES_ROOT -> module = module.copy(usesRoot = stringArr[1].trim())
                    USES_ADMIN -> module = module.copy(usesAdmin = stringArr[1].trim())
                    START_WHEN -> module = module.copy(startWhen = stringArr[1].trim())
                    RESULT_FILE -> module = module.copy(result = stringArr[1].trim())
                    else -> {
                        modulesInt[module.name] = module
                        module = Module()
                    }
                }
            }

        return modulesInt
    }

    private fun genJson(key: String, module: ModuleInterface): String {
        return """
            {
                "name": "$key",
                "version": "${module.version()}",
                "author": "${module.author()}",
                "desc": "${module.desc()}",
                "startWhen": "${module.startWhen().joinToString(" ")}",
                "command": "${module.command()}",
                "result": "${module.result()}",
                "resultType": "${module.resultType()}",
                "usesAdmin": "${module.usesAdmin()}",
                "usesRoot": ${module.usesRoot()},
            }
        """.trimIndent()
    }
}