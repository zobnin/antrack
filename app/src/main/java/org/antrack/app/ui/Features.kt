package org.antrack.app.ui

import org.antrack.app.C
import org.antrack.app.Init
import org.antrack.app.libs.L
import java.io.BufferedReader
import java.io.FileReader

class Features(dir: String) {
    var root = false
    var admin = false
    var backCamera = false
    var frontCamera = false
    var phone = false

    init {
        try {
            val reader = BufferedReader(FileReader(
                    Init.DEVICES_DIR + "/" + dir + C.FEATURES_FILE))
            reader.readLines().forEach {
                when (it.trim { it <= ' ' }) {
                    "root" -> root = true
                    "admin" -> admin = true
                    "backCamera" -> backCamera = true
                    "frontCamera" -> frontCamera = true
                    "phone" -> phone = true
                }
            }
        } catch (e: Exception) {
            L.e(TAG, "Read exception: " + e.toString())
        }
    }

    companion object {
        private val TAG = "UI/Features"
    }
}
