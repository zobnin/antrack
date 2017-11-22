package org.antrack.app

import org.antrack.app.libs.L
import java.util.*
import java.util.concurrent.ConcurrentHashMap

// Watch for control file changes in cloud
object CloudWatcher {
    private const val TAG = "CloudWatcher"
    private val watchers: HashMap<String, Watcher> = HashMap()

    interface Callback {
        fun onFileUpdate(path: String)

        val watchFile: String?
    }

    private class Watcher internal constructor(private val device: String) {
        internal val callbacks: ConcurrentHashMap<String, Callback> = ConcurrentHashMap<String, Callback>()
        private var active = false

        internal fun addCallback(name: String, callback: Callback) {
            callbacks.put(name, callback)
        }

        internal fun removeCallback(name: String) {
            callbacks.remove(name)
        }

        internal fun startWatching() {
            active = true
            Thread(Runnable {
                L.d(TAG, "Start thread for device: " + device)

                while (active) {
                    try {
                        // Sleep if there are no internet connection
                        Pw.waitOnline()
                        val changedFiles = Pw.watchForChanges("/" + device)

                        // Second check if thread become inactive while blocked
                        if (!active)
                            break

                        if (changedFiles != null) {
                            for (path in changedFiles) {
                                processFile(path)
                            }
                        }
                    } catch (e: Exception) {
                        L.e(TAG, "Thread interrupted")
                        break
                    }

                }
            }).start()
        }

        internal fun stopWatching() {
            active = false
        }
    }

    fun addCallback(name: String, callback: Callback?) {
        if (callback == null) {
            L.e(TAG, "addCallback: callback = null")
            return
        }

        callback.watchFile?.let {
            val device = it.split("/".toRegex())[1]

            if (!watchers.containsKey(device)) {
                val watcher = Watcher(device)
                watcher.addCallback(name, callback)
                watchers.put(device, watcher)
                watcher.startWatching()
            } else {
                watchers[device]?.addCallback(name, callback)
            }

            L.d(TAG, "addCallback name: " + name + ", device: " + device + ", file: " + callback.watchFile)
        }
    }

    fun removeCallback(name: String) {
        val it = watchers.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            val watcher = entry.value

            if (watcher.callbacks.containsKey(name)) {
                watcher.removeCallback(name)
                if (watcher.callbacks.isEmpty()) {
                    watcher.stopWatching()
                    it.remove()
                }
            }
        }

        L.d(TAG, "removeCallback name: " + name)
    }

    private fun processFile(path: String) {
        val device = path.split("/".toRegex())[1]

        L.d(TAG, "File modified, device: $device, path: $path")

        watchers[device]?.let {
            it.callbacks.values
                    .filter { path.contains(it.watchFile!!) }
                    .forEach { it.onFileUpdate(path) }
        }
    }
}

