package org.antrack.app.service

import org.antrack.app.DONE
import org.antrack.app.Env
import org.antrack.app.ON
import org.antrack.app.functions.className
import org.antrack.app.functions.logD
import org.antrack.app.functions.touch
import org.antrack.app.libs.Admin
import java.io.File

class InternalCommands {
    private val admin = Admin()

    fun wipeDevice(): String {
        try {
            admin.wipe()
        } catch (e: Exception) {
            return "Error: ${e.message}"
        }
        return DONE
    }

    fun lockDevice(): String {
        try {
            admin.lock()
        } catch (e: Exception) {
            return "Error: ${e.message}"
        }
        return DONE
    }

    fun markAsLost(switch: String): String {
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