@file:Suppress("DEPRECATION")

package org.antrack.app.libs

import android.os.FileObserver
import java.io.File
import java.util.*

open class RecursiveFileObserver(
    private val path: String,
    private val mask: Int = CHANGES_ONLY
) : FileObserver(
    path, mask
) {
    companion object {
        private const val CHANGES_ONLY = CLOSE_WRITE or MOVED_TO
    }

    private val observers = mutableListOf<SingleFileObserver>()

    override fun startWatching() {
        val stack = Stack<String>().apply {
            push(path)
        }

        while (!stack.empty()) {
            val parent = stack.pop()
            observers.add(SingleFileObserver(parent, mask))

            val path = File(parent)
            val files = path.listFiles() ?: continue

            files
                .filter { it.isRegularDirectory }
                .forEach { stack.push(it.path) }
        }

        observers.forEach {
            it.startWatching()
        }
    }

    override fun stopWatching() {
        observers.forEach {
            it.stopWatching()
        }
        observers.clear()
    }

    override fun onEvent(event: Int, path: String?) {}

    private inner class SingleFileObserver(
        private val mPath:
        String, mask: Int
    ) : FileObserver(mPath, mask) {

        override fun onEvent(event: Int, path: String?) {
            val newPath = "$mPath/$path"
            this@RecursiveFileObserver.onEvent(event, newPath)
        }
    }

    private val File.isRegularDirectory: Boolean
        get() = isDirectory &&
                name != "." &&
                name != ".."
}