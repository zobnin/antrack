package org.antrack.app.watcher

import org.antrack.app.Env
import org.antrack.app.LOG_FILE
import org.antrack.app.functions.className
import org.antrack.app.functions.logD
import org.antrack.app.libs.RecursiveFileObserver
import java.util.concurrent.ConcurrentHashMap

object FileWatcher : IWatcher, RecursiveFileObserver(Env.mainDirPath) {
    var multithreded = true

    override val callbacks = ConcurrentHashMap<String, IWatcherCallback>()

    override fun onEvent(event: Int, path: String?) {
        processFile(path)
    }

    private fun processFile(filePath: String?) {
        if (filePath == null) return
        if (filePath.endsWith(LOG_FILE)) return

        if (multithreded) {
            Env.executor.submit {
                if (multithreded) {
                    executeCallbacks(filePath)
                }
            }
        } else {
            executeCallbacks(filePath)
        }
    }

    private fun executeCallbacks(filePath: String) {
        val path = filePath.replace("//", "/")

        callbacks.values
            .filter { path.contains(it.watchFile) }
            .forEach { it.onFileUpdated(path) }

        logD(className, "File modified, path: $path")
    }

    fun waitForFile(
        id: String,
        fileName: String,
        block: () -> Unit,
    ) {
        class InitCallback : IWatcherCallback {
            override val watchFile = fileName
            override fun onFileUpdated(path: String) {
                try {
                    removeCallback(id)
                    block()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        addCallback(id, InitCallback())
    }
}
