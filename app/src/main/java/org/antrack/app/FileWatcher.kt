package org.antrack.app

import org.antrack.app.libs.L
import org.antrack.app.libs.RecursiveFileObserver
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object FileWatcher {
    private val TAG = "FileWatcher"

    private val watchers: HashMap<String, Watcher>

    init {
        watchers = HashMap<String, Watcher>()
    }

    interface Callback {
        fun onFileUpdate(path: String)
        // As first dir file must contain device dir
        val watchFile: String?
    }

    private class Watcher internal constructor(device: String) : RecursiveFileObserver(Init.DEVICES_DIR + device) {
        // ConcurrentModificationException workaround
        internal val callbacks: ConcurrentHashMap<String, Callback> = ConcurrentHashMap<String, Callback>()

        internal fun addCallback(name: String, callback: Callback) {
            callbacks.put(name, callback)
        }

        internal fun removeCallback(name: String) {
            callbacks.remove(name)
        }

        override fun onEvent(event: Int, path: String?) {
            if (path == null) {
                return
            }
            processFile(path)
        }
    }

    @Synchronized fun addCallback(name: String, callback: Callback?) {
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
                watchers[device]!!.addCallback(name, callback)
            }

            L.d(TAG, "addCallback name: " + name + ", device: " + device + ", file: " + callback.watchFile)
        }

    }

    @Synchronized fun removeCallback(name: String) {
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

    // FIXME штука в том, что callback может быть добавлен/удален пока выполняется processFile
    private fun processFile(path: String) {
        var path = path
        path = path.replace("//", "/")
        val device = path.replace(Init.DEVICES_DIR, "/").split("/".toRegex())[1]

        L.d(TAG, "File modified, device: $device, path: $path")

        val watcher = watchers[device] ?: return

        watcher.callbacks.values
                .filter {
                    // проблема в том, что к моменту вызова callback'а фрагмент просто может быть еще не загружен
                    it.watchFile != null && path.contains(it.watchFile!!)
                }
                .forEach { it.onFileUpdate(path) }
    }

}
