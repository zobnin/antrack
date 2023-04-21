package org.antrack.app.modules

import org.antrack.app.App
import org.antrack.app.Env
import org.antrack.app.functions.className
import org.antrack.app.functions.logD
import org.antrack.app.functions.logE
import org.antrack.app.libs.Admin
import org.antrack.app.libs.Shell
import java.io.File

object Modules {
    private val modules by lazy { loadModules() }

    fun get(): Map<String, ModuleInterface> {
        return modules
    }

    fun command(moduleName: String, args: List<String>): String {
        val module = modules[moduleName] ?: return "error: no such module"

        if (module.usesRoot() && !checkForRoot()) return "error: no root rights"
        if (module.usesAdmin() && !checkForAdmin()) return "error: no admin rights"

        return execCommand(module, args)
    }

    fun run(action: String, extra: String) {
        logD(className, "Get action: $action")

        val haveRoot = checkForRoot()
        val haveAdmin = checkForAdmin()

        modules.forEach { (_, module) ->
            if (module.usesRoot() && !haveRoot) return@forEach
            if (module.usesAdmin() && !haveAdmin) return@forEach

            execAction(module, action, extra)
        }
    }

    private fun execCommand(
        module: ModuleInterface,
        args: List<String>
    ): String {
        return try {
            module.onCommand(App.context, args.toTypedArray())
        } catch (e: Exception) {
            e.printStackTrace()
            "error: ${e.message.toString()}"
        }
    }

    private fun execAction(
        module: ModuleInterface,
        action: String,
        extra: String
    ) {
        try {
            when (action) {
                "boot" -> module.onBoot(App.context)
                "alarm" -> module.onAlarm(App.context)
                "screenOn" -> module.onScreenOn(App.context)
                "incomingCall" -> module.onIncomingCall(App.context, extra)
                "outgoingCall" -> module.onOutgoingCall(App.context, extra)
                "load" -> processLoadAction(module)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            logE(className, "error: ${e.message}")
        }
    }

    private fun processLoadAction(module: ModuleInterface) {
        if (
            module.result() != null &&
            module.result().endsWith("/")
        ) {
            File(Env.mainDirPath + module.result()).mkdirs()
        }

        module.onLoad(App.context)
    }

    private fun checkForRoot(): Boolean {
        return Shell.checkSu()
    }

    private fun checkForAdmin(): Boolean {
        return Admin().isActive
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
}
