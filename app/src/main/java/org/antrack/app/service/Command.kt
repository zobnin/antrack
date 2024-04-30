package org.antrack.app.service

import org.antrack.app.modules.Modules

class Command(
    val command: String,
) {
    private sealed class Type(
        val startSymbol: String,
    ) {
        data object Regular : Type(startSymbol = "")
        data object Silent : Type(startSymbol = "!")
        data object Testing : Type(startSymbol = "@")
    }

    private val type: Type
        get() = when {
            command.startsWith(Type.Silent.startSymbol) -> Type.Silent
            command.startsWith(Type.Testing.startSymbol) -> Type.Testing
            else -> Type.Regular
        }

    fun execute() {
        checkForErrors()?.let {
            handleError(it)
            return
        }

        command
            .splitMultiCommand()
            .forEach { (cmd, args) ->
                executeSingleCommand(cmd, args)
            }
    }

    private fun checkForErrors(): String? {
        return when (command.length) {
            in 2..200 -> null
            else -> "command should be 2..200 symbols"
        }
    }

    private fun executeSingleCommand(cmd: String, args: String) {
        val result = when {
            InternalCommands.isInternal(cmd) -> InternalCommands.run(cmd, args)
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

    private fun handleError(errorStr: String) {
        when (type) {
            is Type.Regular -> Files.writeErrorResult(errorStr)
            is Type.Testing -> Files.writeTestErrorResult(errorStr)
            is Type.Silent -> { /* silent */ }
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
