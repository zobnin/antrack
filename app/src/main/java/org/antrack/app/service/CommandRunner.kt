package org.antrack.app.service

import org.antrack.app.ACCURATE_TIME_FORMAT
import org.antrack.app.App
import org.antrack.app.BOOTSTRAP_ASSET
import org.antrack.app.Env
import org.antrack.app.functions.className
import org.antrack.app.functions.formatDate
import org.antrack.app.functions.logD
import org.antrack.app.modules.Modules
import splitMultiCommand
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*

class CommandRunner {
    private val intCommands = InternalCommands()
    private var internalCommand = false

    fun executeModules(action: String, extra: String) {
        Modules.run(action, extra)
    }

    fun executeBootstrap() {
        val iStream = App.context.assets.open(BOOTSTRAP_ASSET)

        BufferedReader(InputStreamReader(iStream)).use { reader ->
            reader.forEachLine { line ->
                if (line.length > 2) {
                    logD(className, "get bootstrap line: $line")
                    executeCommand(line.trim())
                }
            }
        }
    }

    fun executeCommand(cmd: String) {
        logD(className, "command: $cmd")

        if (cmd.length > 200) {
            writeResult("internal", "error: command can't be > 200 symbols")
            return
        }

        internalCommand = cmd.startsWith("!")

        cmd.splitMultiCommand()
            .forEach { (cmd, args) ->
                executeSingleCommand(cmd, args)
            }
    }

    private fun executeSingleCommand(cmd: String, args: String) {
        val result = when {
            intCommands.isInternal(cmd) -> intCommands.run(cmd, args)
            else -> Modules.command(cmd, args.split(" "))
        }

        if (!internalCommand) {
            writeResult(cmd, result)
        }
    }

    private fun writeResult(cmd: String, result: String) {
        File(Env.resultFilePath).apply {
            val date = formatDate(Date().time, ACCURATE_TIME_FORMAT)
            writeText("$date\n$cmd $result\n")
        }
    }
}
