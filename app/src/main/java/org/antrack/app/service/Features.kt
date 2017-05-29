package org.antrack.app.service

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import org.antrack.app.C
import org.antrack.app.Settings
import org.antrack.app.libs.L
import java.io.FileWriter

class Features {
    var root = false
    var admin = false
    var backCamera = false
    var frontCamera = false
    var phone = false

    fun write(context: Context, path: String) {
        getFeatures(context)

        try {
            val writer = FileWriter(path)

            var feat = ""
            if (root) feat += "root\n"
            if (admin) feat += "admin\n"
            if (backCamera) feat += "backCamera\n"
            if (frontCamera) feat += "frontCamera\n"
            if (phone) feat += "phone\n"

            writer.write(feat)
            writer.close()
        } catch (e: Exception) {
            L.e(TAG, "Write error: " + e.toString())
        }
    }

    private fun getFeatures(context: Context) {
        // Do we have root?
        val haveRoot = Settings[C.S_USE_ROOT]
        if (haveRoot == C.TRUE) {
            root = true
        }

        // Do we have admin?
        val haveAdmin = Settings[C.S_USE_ADMIN]
        if (haveAdmin == C.TRUE) {
            root = true
        }

        // Do we have cameras?
        val cameras = Camera.getNumberOfCameras()
        if (cameras > 0) {
            backCamera = true
        }
        if (cameras > 1) {
            frontCamera = true
        }

        // Do we have phone?
        val pm = context.packageManager
        phone = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
    }

    companion object {
        private val TAG = "Service/Features"
    }
}
