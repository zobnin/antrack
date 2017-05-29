package org.antrack.app.ui.callbacks

import org.antrack.app.C
import org.antrack.app.CloudWatcher
import org.antrack.app.Init
import org.antrack.app.Pw
import org.antrack.app.libs.L
import org.antrack.app.ui.State

// Callback for update files from cloud
class CloudCallback : CloudWatcher.Callback {
    override fun onFileUpdate(path: String) {
        if (path.endsWith(C.RESULT_FILE)) {
            try {
                Pw.getFile(Init.DEVICES_DIR + path, path)
            } catch (e: Exception) {
                L.d("CloudCallback", "Error downloading result: " + e.toString())
            }

        } else if (State.fragment != null) {
            val watchFile = State.fragment!!.watchFile
            if (watchFile != null) {
                if (path.contains(watchFile)) {
                    Thread(Runnable {
                        try {
                            if (Pw.isConnected)
                                Pw.getFile(Init.DEVICES_DIR + path, path)
                        } catch (e: Exception) {
                            L.d("CloudCallback", "Error downloading file: " + e.toString())
                        }
                    }).start()
                }
            }
        }
    }

    override val watchFile: String?
        get() = "/" + State.device!!.dir + "/"
}
