package org.antrack.app.service

import org.antrack.app.C
import org.antrack.app.Init
import org.antrack.app.Pw
import org.antrack.app.Settings
import org.antrack.app.libs.Files
import org.antrack.app.libs.L
import org.antrack.app.libs.Utils
import java.io.File
import java.io.IOException
import java.util.*

object U {
    @Throws(IOException::class)
    internal fun parseCtl() {
        val command = Files.readTextFile(getFullPath(C.CONTROL_FILE)).trim { it <= ' ' }
        CC.parseCommand(command)
    }

    @Throws(IOException::class)
    internal fun parseCtlq() {
        val cmds = Files.textFileToArray(getFullPath(C.CONTROL_Q_FILE))

        val lastCmdTime = java.lang.Long.parseLong(Settings[C.S_LAST_CMD_TIME])

        for (cmd in cmds) {
            val cmdA = cmd.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val cmdTime = cmdA[0].trim { it <= ' ' }
            val cmdName = Utils.arrayToString(Arrays.copyOfRange(cmdA, 1, cmdA.size))

            if (java.lang.Long.parseLong(cmdTime) > lastCmdTime) {
                Settings.put(C.S_LAST_CMD_TIME, cmdTime)
                // This action may crash if module has errors
                CC.parseCommand(cmdName)
            }
        }
    }

    @Throws(InterruptedException::class)
    fun getFile(file: String) {
        if (Pw.isConnected)
            Pw.getFile(U.getFullPath(file), U.getCloudPath(file))
    }

    @Throws(InterruptedException::class)
    fun putFile(file: String) {
        if (Pw.isConnected)
            Pw.putFile(U.getFullPath(file), U.getCloudPath(file), false)
    }

    // Upload file with full path
    @Throws(InterruptedException::class)
    internal fun uploadFile(path: String) {
        if (!Pw.isConnected)
            return

        val file = File(path)

        if (file.isDirectory) {
            return
        }

        if (!file.exists()) {
            L.e("U/uploadFile", "File don't exist: " + path)
            return
        }

        Pw.putFile(path, "/" + path.replace(Init.DEVICES_DIR, ""), false)
    }

    // Get full local dir path
    private fun getFullPath(path: String): String {
        return Init.MAIN_DIR + path
    }

    // Get full cloud dir path
    private fun getCloudPath(path: String): String {
        return "/" + Init.DEVICE_NAME_IMEI + path
    }

}
