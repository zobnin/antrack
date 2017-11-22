package org.antrack.app.service

import org.antrack.app.App
import org.antrack.app.C
import org.antrack.app.Init
import org.antrack.app.Pw
import org.antrack.app.libs.Files
import org.antrack.app.libs.L
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

object TrustedDevices {
    private const val TAG = "TrustedDevices"
    private var prop: Properties? = null
    private val trustedFile: String

    init {
        val context = App.context

        trustedFile = context!!.applicationInfo.dataDir + C.TRUSTED_DEVICES_FILE
        Files.touch(trustedFile)

        try {
            prop = Properties()
            prop!!.load(FileInputStream(trustedFile))
        } catch (e: Exception) {
            L.e(TAG, e.toString())
        }
    }

    fun ban(deviceName: String) {
        putKey(deviceName, "banned")
    }

    fun trust(deviceName: String): Boolean {
        Files.mkdir(Init.DEVICES_DIR + deviceName)

        if (Pw.isConnected) {
            try {
                Pw.getFile(Init.DEVICES_DIR + deviceName + C.PUBLIC_KEY_FILE,
                        "/" + deviceName + C.PUBLIC_KEY_FILE)
            } catch (e: Exception) {
                L.e(TAG, "Can't download public key: " + e.toString())
                return false
            }

        } else {
            L.e(TAG, "Can't download public key: not connected")
            return false
        }

        try {
            val stringKey = Files.readTextFile(
                    Init.DEVICES_DIR + deviceName + C.PUBLIC_KEY_FILE)
            putKey(deviceName, stringKey)
        } catch (e: Exception) {
            L.e(TAG, "Can't read public key: " + e.toString())
            return false
        }

        return true
    }

    private fun putKey(name: String, value: String) {
        prop!!.setProperty(name, value)
        store()
        L.d(TAG, "Add trusted device: $name = $value")
    }

    fun getKey(name: String): String {
        val value = prop!!.getProperty(name)
        L.d(TAG, "Get trusted device: $name = $value")
        return value
    }

    private fun store() {
        try {
            prop!!.store(FileOutputStream(trustedFile), "")
        } catch (e: Exception) {
            L.e(TAG, e.toString())
        }
    }
}
