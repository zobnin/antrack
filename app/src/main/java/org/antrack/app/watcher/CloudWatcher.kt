package org.antrack.app.watcher

import org.antrack.app.Env
import org.antrack.app.cloud.Cloud
import org.antrack.app.functions.className
import org.antrack.app.functions.logD
import org.antrack.app.functions.logE
import java.util.concurrent.ConcurrentHashMap

// Watch for control file changes in cloud
object CloudWatcher : IWatcher {
    override val callbacks = ConcurrentHashMap<String, IWatcherCallback>()
    var active = false

    override fun addCallback(name: String, callback: IWatcherCallback) {
        callbacks[name] = callback
    }

    override fun removeCallback(name: String) {
        callbacks.remove(name)
    }

    override fun startWatching() {
        startWatcherThread()
    }

    override fun stopWatching() {
        active = false
    }

    private fun startWatcherThread() {
        active = true
        Thread {
            logD(className, "Start thread for device: ${Env.deviceNameId}")

            while (active) {
                try {
                    // Sleep if there are no internet connection
                    Cloud.waitOnline()
                    val changedFiles = Cloud.watchForChanges("/${Env.deviceNameId}")

                    // Second check if thread become inactive while blocked
                    if (!active) break

                    if (changedFiles != null) {
                        for (path in changedFiles) {
                            processFile(path)
                        }
                    }
                } catch (e: Exception) {
                    logE(className, "Thread interrupted")
                    break
                }
            }
        }.start()
    }

    private fun processFile(path: String) {
        val device = path.split("/".toRegex())[1]

        logD(className, "File modified, device: $device, path: $path")

        callbacks.values
            .filter { path.contains(it.watchFile) }
            .forEach { it.onFileUpdated(path) }
    }
}

