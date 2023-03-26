package org.antrack.app.service.watcher

import org.antrack.app.CONTROL_FILE
import org.antrack.app.CONTROL_Q_FILE
import org.antrack.app.functions.className
import org.antrack.app.functions.logE
import org.antrack.app.service.CommandRunner
import org.antrack.app.service.Files
import org.antrack.app.watcher.IWatcherCallback

class LocalCtlChangeCallback(
    private val runner: CommandRunner,
) : IWatcherCallback {

    override val watchFile: String
        get() = CONTROL_FILE

    override fun onFileUpdated(path: String) {
        if (path.isEmpty()) return

        try {
            when {
                // Current device ctl changed -> read and execute command
                path.endsWith(CONTROL_FILE) -> parseCtl()
                // Current device ctlq changed -> read and execute commands
                path.endsWith(CONTROL_Q_FILE) -> parseCtlq()
                // Other file changed -> do nothing
                else -> {}
            }
        } catch (e: Exception) {
            logE(className, "Error: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun parseCtl() {
        val command = Files.readCtlFile()
        runner.executeCtlCommand(command)
    }

    private fun parseCtlq() {
        Files.readCtlqFile().forEach { cmd ->
            executeCtlqCommand(cmd)
        }
    }

    private fun executeCtlqCommand(cmd: String) {
        try {
            runner.executeCtlqCommand(cmd)
        } catch (e: Exception) {
            logE(className, "Error: ${e.message}")
            e.printStackTrace()
        }
    }
}