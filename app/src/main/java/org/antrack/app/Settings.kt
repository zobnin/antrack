package org.antrack.app

import org.antrack.app.libs.Files
import org.antrack.app.libs.L
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

object Settings {
    private val TAG = "Settings"

    private var prop: Properties = Properties()
    private var settingsFile = Init.MAIN_DIR + C.SETTINGS_FILE

    init {
        Files.touch(settingsFile)

        try {
            prop.load(FileInputStream(settingsFile))
        } catch (e: Exception) {
            L.e(TAG, e.toString())
        }
    }

    fun saveToken(token: String) {
        try {
            Files.writeTextFile(App.context!!.applicationInfo.dataDir + C.TOKEN_FILE, token)
        } catch (e: IOException) {
            L.e(TAG, "Can't save token: " + e.toString())
        }

    }

    fun readToken(): String? {
        var token: String? = null

        try {
            token = Files.readTextFile(App.context!!.applicationInfo.dataDir + C.TOKEN_FILE)
        } catch (e: IOException) {
            L.e(TAG, "Can't read token: " + e.toString())
        }

        return token
    }

    fun put(name: String, value: String) {
        prop.setProperty(name, value)
        // For cloud synchronization
        store()
        L.d(TAG, "Set settings: $name = $value")
    }

    operator fun get(name: String): String? {
        val value = prop.getProperty(name)
        L.d(TAG, "Get settings: $name = $value")
        return value
    }

    private fun store() {
        try {
            prop.store(FileOutputStream(settingsFile), "")
        } catch (e: Exception) {
            L.e(TAG, e.toString())
        }

    }
}
