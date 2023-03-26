package org.antrack.app.service

import org.antrack.app.DONE
import org.antrack.app.Env
import org.antrack.app.ON
import org.antrack.app.functions.className
import org.antrack.app.functions.logD
import org.antrack.app.functions.touch
import org.antrack.app.libs.Admin
import org.antrack.app.modules.ModulesSerializer
import java.io.File

object InternalCommands {
    private const val LOST = "lost"
    private const val MODULES = "modules"
    private const val DUMP_JSON = "dumpjson"
    private const val LOCK = "lock"
    private const val WIPE = "wipe"

    private val admin = Admin()
    private val modSerializer = ModulesSerializer()

    private val commands = listOf(
        LOST, LOCK, MODULES, DUMP_JSON, LOCK, WIPE
    )

    fun isInternal(cmd: String): Boolean {
        return commands.contains(cmd)
    }

    fun run(cmd: String, args: String) = when (cmd) {
        LOST -> markAsLost(args)
        LOCK -> lockDevice()
        WIPE -> wipeDevice()
        MODULES -> modSerializer.write()
        DUMP_JSON -> modSerializer.writeJson()
        else -> "error: command not found"
    }

    private fun wipeDevice(): String {
        try {
            admin.wipe()
        } catch (e: Exception) {
            return "error: ${e.message}"
        }
        return DONE
    }

    private fun lockDevice(): String {
        try {
            admin.lock()
        } catch (e: Exception) {
            return "error: ${e.message}"
        }
        return DONE
    }

    private fun markAsLost(switch: String): String {
        if (switch == ON) {
            File(Env.lostFilePath).touch()
            logD(className, "Marked as lost")
        } else {
            File(Env.lostFilePath).delete()
            logD(className, "Marked as not lost")
        }
        return DONE
    }
}