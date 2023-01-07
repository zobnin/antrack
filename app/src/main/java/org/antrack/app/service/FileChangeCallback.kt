package org.antrack.app.service

import org.antrack.app.CONTROL_FILE
import org.antrack.app.CONTROL_Q_FILE
import org.antrack.app.Env
import org.antrack.app.Settings
import org.antrack.app.cloud.Cloud
import org.antrack.app.functions.className
import org.antrack.app.functions.logE
import org.antrack.app.functions.readAsList
import org.antrack.app.watcher.IWatcherCallback
import java.io.File

// Callback waits for local file changes
class FileChangeCallback(
    private val cc: CommandRunner,
) : IWatcherCallback {

    override val watchFile: String
        get() = "/${Env.deviceNameId}/"

    override fun onFileUpdated(path: String) {
        if (path.isEmpty()) return

        try {
            when {
                // Current device ctl changed -> read and execute command
                path.endsWith(CONTROL_FILE) -> parseCtl()
                // Current device ctlq changed -> read and execute commands
                path.endsWith(CONTROL_Q_FILE) -> parseCtlq()
                // Other file changed -> upload to cloud
                else -> uploadFile(path)
            }
        } catch (e: Exception) {
            logE(className, "Error: ${e.message}")
        }
    }

    private fun uploadFile(path: String) {
        if (!Cloud.isConnected) return

        val file = File(path)
        if (file.isDirectory) {
            return
        }

        if (!file.exists()) {
            throw IllegalArgumentException("File don't exist $path")
        }

        Cloud.putFile(path, path.replace(Env.appDirPath, ""))
    }

    private fun parseCtl() {
        val command = File(Env.ctlFilePath).readText()
        cc.executeCommand(command)
    }

    private fun parseCtlq() {
        File(Env.ctlqFilePath)
            .readAsList()
            .forEach { cmd ->
                processCtlqCommand(cmd)
            }
    }

    private fun processCtlqCommand(cmd: String) {
        val cmdA = cmd
            .split(" ", limit = 2)
            .dropLastWhile { it.isEmpty() }

        val cmdTime = cmdA[0].toLong()
        val command = cmdA[1]

        if (cmdTime > Settings.lastCommandTime) {
            Settings.lastCommandTime = cmdTime
            cc.executeCommand(command)
        }
    }
}
