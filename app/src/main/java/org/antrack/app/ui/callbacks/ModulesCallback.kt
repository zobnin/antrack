package org.antrack.app.ui.callbacks

import org.antrack.app.C
import org.antrack.app.FileWatcher
import org.antrack.app.libs.LoadingDialog
import org.antrack.app.ui.MainActivity
import org.antrack.app.ui.State

// Callback for update modules
class ModulesCallback(private val activity: MainActivity) : FileWatcher.Callback {
    init {
        active = true
    }

    override fun onFileUpdate(path: String) {
        FileWatcher.removeCallback("modules")
        active = false

        if (FeaturesCallback.active) return
        if (OsidCallback.active) return
        if (KeyCallback.active) return

        activity.runOnUiThread {
            activity.switchDevice(true)
            LoadingDialog.hide(activity)
        }
    }

    override val watchFile: String?
        get() = "/" + State.device!!.dir + C.MODULES_FILE

    companion object {
        internal var active = false
    }
}
