package org.antrack.app.service

import org.antrack.app.App
import org.antrack.app.C
import org.antrack.app.Init
import org.antrack.app.Settings
import org.antrack.app.libs.Files
import org.antrack.app.libs.L
import org.antrack.app.libs.Utils
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

object CC {
    private const val TAG = "CC"
    private var internalCommand = false

    fun runModules(action: String, extra: String) {
        Modules.run(action, extra)
    }

    fun parseBootstrap() {
        try {
            val `is` = App.context!!.assets.open(C.BOOTSTRAP_ASSET)
            val reader = BufferedReader(InputStreamReader(`is`))
            for (line in reader.readLines()) {
                if (line.length > 2) {
                    L.d(TAG, "get bootstrap line: " + line)
                    parseCommand(line.trim { it <= ' ' })
                }
            }
            reader.close()
        } catch (e: IOException) {
            L.e(TAG, "unpackBootstrap error: " + e.toString())
        }

    }

    fun parseCommand(command: String) {
        var cmd = command

        L.d(TAG, "command: $cmd")

        if (cmd.length > 200) {
            writeResult("internal", "error: command can't be > 200 symbols")
            return
        }

        // Commands that start with "!" are internal: no write /result, no logged
        if (cmd.startsWith("!")) {
            cmd = cmd.substring(1)
            internalCommand = true
        } else {
            internalCommand = false
        }

        // Parse multi-command
        val cmds = if (cmd.contains(";")) {
            cmd.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        } else {
            arrayOf(cmd)
        }

        // Parse command
        cmds
                .map { oneCmd -> oneCmd.trim { it <= ' ' } }
                .map { oneCmdTrim -> oneCmdTrim.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray() }
                .forEach {
                    when (it[0]) {
                    // Activate "lost" mode
                        "lost" -> writeResult(it[0], lost(it[1]))
                    // Dump /modules
                        "modules" -> writeResult(it[0], Modules.listModules())
                    // Dump /modules.json
                        "dumpjson" -> writeResult(it[0], Modules.dumpJSON())
                    // Run module onCommand
                        else -> {
                            val result = Modules.command(it[0], Arrays.copyOfRange(it, 1, it.size))
                            writeResult(it[0], result)
                        }
                    }
                }

        if (!internalCommand) {
            Logger.runCommand(cmd)
        }
    }

    private fun writeResult(cmd: String, result: String?) {
        if (result != null && !internalCommand) {
            try {
                Files.writeTextFile(Init.RESULT_FILE, Utils.date(C.ACCURATE_TIME_FORMAT))
                Files.addLine(Init.RESULT_FILE, cmd + " " + result)
            } catch (e: IOException) {
                L.e(TAG, "writeResult IOException: " + e)
            }

        }
    }

    private fun lost(pin: String): String {
        /*
         * 1. Hide app - done
         * 2. Enable password
         * 3. Remove other devices files
         * 4. Turn on setting "photo on screen on" - done
         */

        if (pin == C.ON) {
            parseCommand("hide on")
            Settings.put(C.S_SCREEN_ON_PHOTO, C.TRUE)
            Logger.lost()
        } else {
            parseCommand("hide off")
            Settings.put(C.S_SCREEN_ON_PHOTO, C.FALSE)
            Logger.unlost()
        }

        return C.DONE
    }

}
