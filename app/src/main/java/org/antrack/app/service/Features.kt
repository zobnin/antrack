package org.antrack.app.service

import android.content.pm.PackageManager
import android.hardware.Camera
import org.antrack.app.App
import org.antrack.app.libs.Shell
import java.io.FileWriter

class Features {
    val root by lazy { Shell.checkSu() }
    val admin = false
    val backCamera by lazy { Camera.getNumberOfCameras() > 0 }
    val frontCamera by lazy { Camera.getNumberOfCameras() > 1 }
    val phone by lazy {
        App.context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
    }

    fun write(path: String) {
        try {
            writeNoCaching(path)
        } catch (e: Exception) {
            throw IllegalStateException("Can't write features file", e)
        }
    }

    private fun writeNoCaching(path: String) {
        FileWriter(path).use { writer ->
            var feat = ""
            if (root) feat += "root\n"
            if (admin) feat += "admin\n"
            if (backCamera) feat += "back_camera\n"
            if (frontCamera) feat += "front_camera\n"
            if (phone) feat += "phone\n"
            writer.write(feat)
        }
    }
}
