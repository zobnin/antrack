package org.antrack.app.modules

import org.antrack.app.App
import org.antrack.app.DONE
import org.antrack.app.Env
import org.antrack.app.MODULES_JSON_FILE
import org.antrack.app.functions.className
import org.antrack.app.functions.logD
import org.antrack.app.functions.logE
import org.antrack.app.libs.Admin
import org.antrack.app.libs.Shell
import java.io.File
import java.io.FileWriter
import java.io.IOException

object Modules {
    private val modules by lazy { loadModules() }

    fun get(): Map<String, ModuleInterface> {
        return modules
    }

    fun command(moduleName: String, args: List<String>): String {
        val module = modules[moduleName]
        if (module != null) {
            if (module.usesRoot() && !checkForRoot()) return "error: no root rights"
            if (module.usesAdmin() && !checkForAdmin()) return "error: no admin rights"

            return try {
                module.onCommand(App.context, args.toTypedArray())
            } catch (e: Exception) {
                e.printStackTrace()
                "error: ${e.message.toString()}"
            }
        }

        return "error: no such module"
    }

    fun run(action: String, extra: String) {
        logD(className, "Get action: $action")

        val haveRoot = checkForRoot()
        val haveAdmin = checkForAdmin()

        modules.forEach { (_, module) ->
            if (module.usesRoot() && !haveRoot) return@forEach
            if (module.usesAdmin() && !haveAdmin) return@forEach

            try {
                when (action) {
                    "boot" -> module.onBoot(App.context)
                    "alarm" -> module.onAlarm(App.context)
                    "screenOn" -> module.onScreenOn(App.context)
                    "incomingCall" -> module.onIncomingCall(App.context, extra)
                    "outgoingCall" -> module.onOutgoingCall(App.context, extra)
                    "load" -> {
                        if (
                            module.result() != null &&
                            module.result().endsWith("/")
                        ) {
                            File(Env.mainDirPath + module.result()).mkdirs()
                        }
                        module.onLoad(App.context)
                    }
                }
            } catch (e: Exception) {
                logE(className, "error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun writeModulesFile(): String {
        if (modules.isEmpty()) return "error: no modules"

        try {
            val writer = FileWriter(Env.modulesFilePath)

            for ((key) in modules) {
                val module = modules[key]

                var info = ""
                info += "Name: $key\n"
                info += "Version: ${module!!.version()}\n"
                info += "Author: ${module.author()}\n"
                info += "Description: ${module.desc()}\n"
                info += "Command: ${module.command()}\n"
                info += "Uses root: ${module.usesRoot()}\n"
                info += "Uses admin: ${module.usesAdmin()}\n"
                info += "Result file: "
                info += when {
                    module.result() == "" -> "none\n"
                    else -> module.result() + "\n"
                }

                info += "Start when: "
                info += when {
                    module.startWhen() != null -> module.startWhen().joinToString(" ")
                    else -> "never"
                }

                info += "\n\n"
                writer.write(info)
            }
            writer.close()
        } catch (e: IOException) {
            logE(className, "error: ${e.message}")
            e.printStackTrace()
        }

        return DONE
    }

    fun writeJsonFile(): String {
        try {
            val fileName = Env.mainDirPath + MODULES_JSON_FILE
            FileWriter(fileName).use { writer ->
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

    private fun loadModules(): Map<String, ModuleInterface> {
        try {
            File(Env.modulesDirPath).mkdirs()
            return ModuleLoader(App.context, Env.modulesDirPath).getModuleObjects()
        } catch (e: Exception) {
            e.printStackTrace()
            logE(className, "Filed to load modules")
        }

        return emptyMap()
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

    private fun checkForRoot(): Boolean {
        return Shell.checkSu()
    }

    private fun checkForAdmin(): Boolean {
        return Admin().isActive
    }
}
