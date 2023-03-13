package org.antrack.app.service

import org.antrack.app.modules.Modules

class Command(
    val command: String,
) {
    private sealed class Type(
        val startSymbol: String,
    ) {
        object Regular: Type("")
        object Silent : Type("!")
        object Testing : Type("@")
    }

    private val type: Type
        get() = when {
            command.startsWith(Type.Silent.startSymbol) -> Type.Silent
            command.startsWith(Type.Testing.startSymbol) -> Type.Testing
            else -> Type.Regular
        }

    fun execute() {
        if (command.length !in 2..200) {
            Files.writeErrorResult("command should be 2..200 symbols")
            return
        }

        command
            .splitMultiCommand()
            .forEach { (cmd, args) ->
                executeSingleCommand(cmd, args)
            }
    }

    private fun executeSingleCommand(cmd: String, args: String) {
        val result = when {
            InternalCmds.isInternal(cmd) -> InternalCmds.run(cmd, args)
            else -> Modules.command(cmd, args.split(" "))
        }

        handleResult(cmd, result)
    }

    private fun handleResult(cmd: String, result: String) {
        when (type) {
            Type.Regular -> Files.writeCmdResult(cmd, result)
            Type.Testing -> Files.writeTestCmdResult(cmd, result)
            Type.Silent -> { /* silent */ }
        }
    }

    private fun String.splitMultiCommand() =
        this.removePrefix(type.startSymbol)
            .split(";")
            .dropLastWhile { it.isEmpty() }
            .map { it.trim() }
            .map { it.split(" ", limit = 2) }
            .associate { it[0] to (it.getOrNull(1) ?: "") }
}
