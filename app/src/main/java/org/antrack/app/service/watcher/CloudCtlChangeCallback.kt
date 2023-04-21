package org.antrack.app.service.watcher

import org.antrack.app.Env
import org.antrack.app.cloud.Cloud
import org.antrack.app.functions.className
import org.antrack.app.functions.logE
import org.antrack.app.watcher.IWatcherCallback

// Callback waits for ctl & ctlq changes
class CloudCtlChangeCallback : IWatcherCallback {

    override val watchFile by lazy { Env.cloudCtlPath }

    override fun onFileUpdated(path: String) {
        try {
            if (Cloud.isConnected) {
                Cloud.getFile(path, Env.appDirPath + path)
            }
        } catch (e: Exception) {
            logE(className, "Error: $e")
            e.printStackTrace()
        }
    }
}

