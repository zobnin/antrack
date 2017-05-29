package org.antrack.app.ui.callbacks

import org.antrack.app.C
import org.antrack.app.FileWatcher
import org.antrack.app.libs.LoadingDialog
import org.antrack.app.ui.MainActivity
import org.antrack.app.ui.State

// Callback for update features
class FeaturesCallback(private val activity: MainActivity) : FileWatcher.Callback {
    init {
        active = true
    }

    override fun onFileUpdate(path: String) {
        FileWatcher.removeCallback("features")
        active = false

        if (ModulesCallback.active) return
        if (OsidCallback.active) return
        if (KeyCallback.active) return

        activity.runOnUiThread {
            activity.switchDevice(true)
            LoadingDialog.hide(activity)
        }
    }

    override val watchFile: String?
        get() = "/" + State.device.dir + C.FEATURES_FILE

    companion object {
        internal var active = false
    }
}

