package org.antrack.app

import org.antrack.app.functions.className
import org.antrack.app.functions.logD
import org.antrack.app.functions.logE
import org.antrack.app.functions.touch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

object Settings {
    private const val S_UPDATE_INTERVAL = "updateInterval" // 30
    private const val S_PLUGIN = "plugin" // null
    private const val S_START_AT_BOOT = "startAtBoot" // true
    private const val S_ENABLE_SERVICE = "enableService" // true
    private const val S_LAST_CMD_TIME = "lastCmdId" // null
    private const val S_FRAGMENT_IDX = "fragmentIdx"

    private var prop: Properties = Properties()
    private var settingsFile = Env.mainDirPath + SETTINGS_FILE

    init {
        File(settingsFile).touch()

        try {
            prop.load(FileInputStream(settingsFile))
        } catch (e: Exception) {
            logE(className, e.toString())
        }
    }

    var isServiceEnabled: Boolean
        get() = get(S_ENABLE_SERVICE) != FALSE
        set(value) = put(S_ENABLE_SERVICE, value.toString())

    var startAtBoot: Boolean
        get() = get(S_START_AT_BOOT) != FALSE
        set(value) = put(S_START_AT_BOOT, value.toString())

    var plugin: String
        get() = get(S_PLUGIN) ?: ""
        set(value) = put(S_PLUGIN, value)

    var updateInterval: Long
        get() = get(S_UPDATE_INTERVAL)?.toLong() ?: DEFAULT_UPDATE_INTERVAL
        set(value) = put(S_UPDATE_INTERVAL, value.toString())

    var lastCommandTime: Long
        get() = get(S_LAST_CMD_TIME)?.toLong() ?: Date().time
        set(value) = put(S_LAST_CMD_TIME, value.toString())

    var token: String
        get() = readToken()
        set(value) = saveToken(value)

    var fragmentId: Int
        get() = get(S_FRAGMENT_IDX)?.toInt() ?: 0
        set(value) = put(S_FRAGMENT_IDX, value.toString())

    private fun put(name: String, value: String) {
        prop.setProperty(name, value)
        // For cloud synchronization
        save()
        logD(className, "Set settings: $name = $value")
    }

    operator fun get(name: String): String? {
        val value = prop.getProperty(name)
        logD(className, "Get settings: $name = $value")
        return value
    }

    private fun saveToken(token: String?) {
        if (token == null) return

        try {
            File(App.dataDir + TOKEN_FILE).writeText(token)
        } catch (e: IOException) {
            logE(className, "Can't save token: $e")
        }
    }

    private fun readToken(): String {
        return try {
            File(App.dataDir + TOKEN_FILE).readText()
        } catch (e: IOException) {
            logE(className, "Can't read token: $e")
            ""
        }
    }

    private fun save() {
        try {
            prop.store(FileOutputStream(settingsFile), "")
        } catch (e: Exception) {
            logE(className, e.toString())
        }
    }
}
