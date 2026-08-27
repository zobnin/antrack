package org.antrack.app.cloud

import android.app.Activity
import java.io.InputStream
import java.io.OutputStream

interface ICloudProvider {
    fun auth(activity: Activity)
    fun resumeAuth(): String
    fun getStatus(): Status
    fun putFile(iStream: InputStream, rFile: String): CloudFileMetadata
    fun getFile(rFile: String, oStream: OutputStream): CloudFileMetadata
    fun putFile(lFile: String, rFile: String): CloudFileMetadata
    fun getFile(rFile: String, lFile: String): CloudFileMetadata
    fun deleteFile(rPath: String, permanent: Boolean = false)
    fun moveFile(rPathFrom: String, rPathTo: String): CloudMetadata
    fun getMetadata(rFile: String): CloudMetadata
    fun listDir(rDir: String, deleted: Boolean = false, dirs: Boolean = false): List<CloudMetadata>
    fun listDirRecursive(rDir: String, fileList: MutableList<CloudMetadata>)
    fun createDir(rDir: String): CloudMetadata
    fun checkForChanges(rDir: String): Boolean
    fun watchForChanges(dir: String): List<String>?

    fun hasSameContent(content: ByteArray, remote: CloudFileMetadata): Boolean = false

    fun putFileBatch(
        files: List<Pair<ByteArray, String>>,
    ): Map<String, CloudFileMetadata> {
        val results = mutableMapOf<String, CloudFileMetadata>()
        for ((content, path) in files) {
            try {
                results[path] = putFile(content.inputStream(), path)
            } catch (_: Exception) {
                // Failed uploads can be retried by the caller.
            }
        }
        return results
    }
}
