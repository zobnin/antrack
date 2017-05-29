package org.antrack.app.ui

import org.antrack.app.C
import org.antrack.app.Init
import org.antrack.app.libs.Crypto
import org.antrack.app.libs.Files
import org.antrack.app.libs.L
import org.antrack.app.ui.U.getLocalPath
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.security.PublicKey

class Device internal constructor(val dir: String) {
    private val TAG = "Device"

    var lastUpdate: String? = null

    val fullPath: String
        get() = Init.DEVICES_DIR + "/" + dir

    val features: Features
        get() = Features(dir)

    val isMain: Boolean by lazy {
        dir == Init.DEVICE_NAME_IMEI
    }

    val name: String by lazy {
        dir.substring(0, dir.lastIndexOf('_')).replace('_', ' ')
    }

    val osId: String by lazy {

        var osIdInt: String = ""

        try {
            osIdInt = Files.readTextFile(Init.DEVICES_DIR + dir + C.OSID_FILE)
        } catch (e: IOException) {
            L.d("Device", "Can't read osid file: " + e.toString())
        }

        osIdInt
    }

    val modules: LinkedHashMap<String, Module> by lazy {

        val modulesInt = LinkedHashMap<String, Module>()

        try {
            val reader = BufferedReader(FileReader(getLocalPath(C.MODULES_FILE)))
            var module = Module()

            reader.readLines()
                    .asSequence()
                    .map { line -> line.split(":".toRegex()).toTypedArray() }
                    .forEach { pair ->
                        when (pair[0]) {
                            "Name" -> module.name = pair[1].trim { it <= ' ' }
                            "Version" -> module.version = pair[1].trim { it <= ' ' }
                            "Author" -> module.author = pair[1].trim { it <= ' ' }
                            "Description" -> module.desc = pair[1].trim { it <= ' ' }
                            "Command" -> module.command = pair[1].trim { it <= ' ' }
                            "Uses root" -> module.usesRoot = pair[1].trim { it <= ' ' }
                            "Uses admin" -> module.usesAdmin = pair[1].trim { it <= ' ' }
                            "Start when" -> module.startWhen = pair[1].trim { it <= ' ' }
                            "Result file" -> {
                                val result = pair[1].trim { it <= ' ' }
                                module.result = result
                                // Make dirs for module
                                if (result.endsWith("/")) {
                                    File(Init.DEVICES_DIR + dir + result).mkdir()
                                }
                            }
                            else -> {
                                modulesInt.put(module.name!!, module)
                                module = Module()
                            }
                        }
                    }
        } catch (e: Exception) {
            L.e(TAG, "Can't read modules file: " + e)
        }

        modulesInt
    }

    // FIXME "!!"
    val publicKey: PublicKey by lazy {

        var key: PublicKey? = null

        try {
            val stringKey = Files.readTextFile(U.getLocalPath(C.PUBLIC_KEY_FILE))
            key = Crypto.stringToPublicKey(stringKey.trim { it <= ' ' })
        } catch (e: Exception) {
            L.d(TAG, "Can't read key file: " + e.toString())
        }

        key!!
    }
}
