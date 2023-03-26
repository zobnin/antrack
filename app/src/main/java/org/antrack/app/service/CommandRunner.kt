package org.antrack.app.service

import org.antrack.app.Settings
import org.antrack.app.functions.className
import org.antrack.app.functions.logD
import org.antrack.app.modules.Modules

class CommandRunner {
    fun executeModules(action: String, extra: String) {
        Modules.run(action, extra)
    }

    fun executeBootstrap() {
        Files.readBootstrap().forEach { line ->
            logD(className, "get bootstrap line: $line")
            if (line.isNotBlank()) {
                executeCtlCommand(line.trim())
            }
        }
    }

    fun executeCtlCommand(cmd: String) {
        logD(className, "command: $cmd")
        Command(cmd).execute()
    }

    fun executeCtlqCommand(cmd: String) {
        val cmdA = cmd
            .split(" ", limit = 2)
            .dropLastWhile { it.isEmpty() }

        val cmdTime = cmdA[0].toLong()
        val command = cmdA[1]

        if (cmdTime > Settings.lastCommandTime) {
            Settings.lastCommandTime = cmdTime
            executeCtlCommand(command)
        }
    }
}
