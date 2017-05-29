package org.antrack.app.ui.callbacks

import org.antrack.app.C
import org.antrack.app.FileWatcher
import org.antrack.app.libs.L
import org.antrack.app.ui.MainActivity
import org.antrack.app.ui.State

import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException

// Callback watching for result file
class ResultCallback(private val activity: MainActivity) : FileWatcher.Callback {

    override fun onFileUpdate(path: String) {
        L.d(TAG, "result updated")

        try {
            val reader = BufferedReader(FileReader(path))

            // FIXME check time
            val time = reader.readLine()
            val result = reader.readLine()

            State.device!!.lastUpdate = time!!.substring(0, time.length - 4)
            State.fragment!!.onResult(result)

        } catch (e: IOException) {
            L.e(TAG, "Can't read result file: " + e.toString())
        }
    }

    override val watchFile: String?
        get() = "/" + State.device!!.dir + C.RESULT_FILE

    companion object {
        private val TAG = "ResultCallback"
    }
}

