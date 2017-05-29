package org.antrack.app.ui.callbacks

import org.antrack.app.FileWatcher
import org.antrack.app.libs.L
import org.antrack.app.ui.State

// Callback for update fragments on file changes
class FragmentCallback : FileWatcher.Callback {
    override fun onFileUpdate(path: String) {
        if (State.fragment != null) {
            State.fragment!!.onFileUpdate()
            L.d("FragmentCallback", "Fragment updated")
        }
    }

    override val watchFile: String?
        get() {
            if (State.fragment != null && State.fragment!!.watchFile != null) {
                return "/" + State.device!!.dir + State.fragment!!.watchFile
            }
            return null
        }
}
