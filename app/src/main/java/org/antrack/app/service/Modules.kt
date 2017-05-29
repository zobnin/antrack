package org.antrack.app.service

import org.antrack.app.*
import org.antrack.app.libs.Files
import org.antrack.app.libs.L
import org.antrack.app.libs.ModuleLoader
import org.antrack.app.libs.Utils
import org.json.simple.JSONObject
import java.io.FileWriter
import java.io.IOException

object Modules {
    private val TAG = "Modules"
    private var modules: Map<String, ModuleInterface>

    init {
        val modulesDir = Init.APP_DIR + C.MODULES_DIR
        val odexDir = Init.APP_DIR + C.ODEX_DIR

        Files.mkdir(modulesDir)
        Files.mkdir(odexDir)

        val ml = ModuleLoader()
        ml.unpackModules(App.context!!, modulesDir)
        modules = ml.getObjects(modulesDir, odexDir)
    }

    fun get(): Map<String, ModuleInterface> {
        return modules
    }

    private fun checkForRoot(): Boolean {
        val useRoot = Settings[C.S_USE_ROOT]
        return !(useRoot.isNullOrEmpty() || useRoot == C.FALSE)
    }

    private fun checkForAdmin(): Boolean {
        val useAdmin = Settings[C.S_USE_ADMIN]
        return !(useAdmin.isNullOrEmpty() || useAdmin == C.FALSE)
    }

    fun command(moduleName: String, args: Array<String>): String {
        val module = modules[moduleName]
        if (module != null) {
            if (module.usesRoot() && !checkForRoot())
                return "error: no root rights"
            if (module.usesAdmin() && !checkForAdmin())
                return "error: no admin rights"

            return module.onCommand(App.context, args)
        }
        return "error: no such module"
    }

    // FIXME иногда modules может быть null, что делать в этом случае?
    fun run(action: String, extra: String) {
        L.d(TAG, "Get action: " + action)

        val root = checkForRoot()
        val admin = checkForAdmin()

        for ((_, module) in modules) {

            if (module.usesRoot() && !root) continue
            if (module.usesAdmin() && !admin) continue

            when (action) {
                "boot" -> module.onBoot(App.context)
                "alarm" -> module.onAlarm(App.context)
                "screenOn" -> module.onScreenOn(App.context)
                "incomingCall" -> module.onIncomingCall(App.context, extra)
                "outgoingCall" -> module.onOutgoingCall(App.context, extra)
                "load" -> {
                    if (module.result() != null && module.result().endsWith("/"))
                        Files.mkdir(Init.MAIN_DIR + module.result())
                    module.onLoad(App.context)
                }
            }
        }
    }

    fun listModules(): String {
        if (modules.isEmpty())
            return "error: no modules"

        try {
            val writer = FileWriter(Init.MAIN_DIR + C.MODULES_FILE)

            for ((key) in modules) {
                val module = modules[key]

                var info = ""
                info += "Name: " + key + "\n"
                info += "Version: " + module!!.version() + "\n"
                info += "Author: " + module.author() + "\n"
                info += "Description: " + module.desc() + "\n"
                info += "Command: " + module.command() + "\n"
                info += "Uses root: " + module.usesRoot() + "\n"
                info += "Uses admin: " + module.usesAdmin() + "\n"
                info += "Result file: "

                if (module.result() == "") {
                    info += "none\n"
                } else {
                    info += module.result() + "\n"
                }

                if (module.startWhen() != null) {
                    info += "Start when: " + Utils.arrayToString(module.startWhen()) + "\n\n"
                } else {
                    info += "Start when: never\n\n"
                }
                writer.write(info)
            }
            writer.close()
        } catch (e: IOException) {
            L.e(TAG, "filed to write modules file: " + e.toString())
        }

        return C.DONE
    }

    fun dumpJSON(): String {
        try {
            val writer = FileWriter(Init.MAIN_DIR + C.MODULES_JSON_FILE)
            for ((key, module) in modules) {
                val obj = JSONObject()
                obj.put("name", key)
                obj.put("version", module.version())
                obj.put("author", module.author())
                obj.put("desc", module.desc())
                obj.put("startWhen", Utils.arrayToString(module.startWhen()))
                obj.put("command", module.command())
                obj.put("result", module.result())
                obj.put("resultType", module.resultType())
                obj.put("usesAdmin", module.usesAdmin())
                obj.put("usesRoot", module.usesRoot())
                writer.write(obj.toJSONString())
                writer.flush()
            }
            writer.close()
            return C.DONE
        } catch (e: IOException) {
            L.e(TAG, "filed to write modules.json file")
            return "error: " + e.toString()
        }

    }

}
