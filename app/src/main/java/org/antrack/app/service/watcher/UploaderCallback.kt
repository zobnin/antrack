package org.antrack.app.service.watcher

import org.antrack.app.CONTROL_FILE
import org.antrack.app.CONTROL_Q_FILE
import org.antrack.app.Env
import org.antrack.app.cloud.Cloud
import org.antrack.app.functions.className
import org.antrack.app.functions.logE
import org.antrack.app.watcher.IWatcherCallback
import java.io.File

// Callback waits for local file changes
class UploaderCallback : IWatcherCallback {

    override val watchFile: String
        get() = "/${Env.deviceNameId}/"

    override fun onFileUpdated(path: String) {
        if (path.isEmpty()) return

        try {
            when {
                // Ctl files watched by another watcher
                path.endsWith(CONTROL_FILE) -> {}
                path.endsWith(CONTROL_Q_FILE) -> {}
                // Other file changed -> upload to cloud
                else -> uploadFile(path)
            }
        } catch (e: Exception) {
            logE(className, "Error: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun uploadFile(path: String) {
        if (!Cloud.isConnected) return

        val file = File(path)
        if (file.isDirectory) {
            return
        }

        if (!file.exists()) {
            throw IllegalArgumentException("File don't exist $path")
        }

        Cloud.putFile(path, path.replace(Env.appDirPath, ""))
    }
}
