package org.antrack.app.libs

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object Shell {
    private val suPaths = arrayOf(
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su"
    )

    fun checkSuRun(): Boolean {
        val uid = run("id", su = true, out = true)
        return uid != null && uid.startsWith("uid=0")
    }

    fun checkSu(): Boolean {
        return suPaths.find { File(it).exists() } != null
    }

    fun run(
        cmd: String?,
        su: Boolean = false,
        out: Boolean = false
    ): String? {

        return try {
            val sh = if (su) "su" else "sh"
            val process = Runtime.getRuntime().exec(arrayOf(sh, "-c", cmd))
            readCommandOutIfNeeded(out, process)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun readCommandOutIfNeeded(out: Boolean, process: Process): String? {
        if (!out) return null

        return BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            var read: Int
            val buffer = CharArray(4096)
            val output = StringBuilder()

            while (reader.read(buffer).also { read = it } > 0) {
                output.append(buffer, 0, read)
            }

            process.waitFor()
            output.toString()
        }
    }
}