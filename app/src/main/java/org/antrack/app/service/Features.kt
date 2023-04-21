package org.antrack.app.service

import org.antrack.app.App
import org.antrack.app.functions.hasBackCamera
import org.antrack.app.functions.hasFrontCamera
import org.antrack.app.functions.hasTelephony
import org.antrack.app.libs.Admin
import org.antrack.app.libs.Shell
import java.io.FileWriter

class Features {
    val root by lazy { Shell.checkSu() }
    val admin: Boolean get() = Admin().isActive
    val backCamera by lazy { App.context.hasBackCamera }
    val frontCamera by lazy { App.context.hasFrontCamera }
    val phone by lazy { App.context.hasTelephony }

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
