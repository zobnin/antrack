package org.antrack.app.service

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
                executeCommand(line.trim())
            }
        }
    }

    fun executeCommand(cmd: String) {
        logD(className, "command: $cmd")
        Command(cmd).execute()
    }
}
