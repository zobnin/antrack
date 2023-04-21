package org.antrack.app.watcher

import org.antrack.app.functions.className
import org.antrack.app.functions.logD
import java.util.concurrent.ConcurrentHashMap

interface IWatcher {
    // Overwrite this
    val callbacks: ConcurrentHashMap<String, IWatcherCallback>
    fun startWatching()
    fun stopWatching()

    fun addCallback(name: String, callback: IWatcherCallback) {
        callbacks[name] = callback
        startWatching()

        logD(className, "addCallback name: " + name + ", file: " + callback.watchFile)
    }

    fun removeCallback(name: String) {
        callbacks.remove(name)

        if (callbacks.isEmpty()) {
            stopWatching()
        }

        logD(className, "removeCallback name: $name")
    }
}