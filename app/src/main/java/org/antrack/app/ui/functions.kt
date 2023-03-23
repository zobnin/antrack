package org.antrack.app.ui

import org.antrack.app.Env
import org.antrack.app.functions.logE
import java.io.File
import java.io.IOException

fun runCommandAsync(cmd: String) {
    try {
        File(Env.ctlFilePath).writeText(cmd)
    } catch (e: IOException) {
        logE("runCommandAsync", "Can't run command $cmd: $e")
    }
}
