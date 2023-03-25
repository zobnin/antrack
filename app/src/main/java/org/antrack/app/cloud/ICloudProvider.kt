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
}