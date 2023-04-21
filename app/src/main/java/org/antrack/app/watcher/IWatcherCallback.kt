package org.antrack.app.watcher

interface IWatcherCallback {
    val watchFile: String
    fun onFileUpdated(path: String)
}