package org.antrack.app.cloud.provider

import android.app.Activity
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.InvalidAccessTokenException
import com.dropbox.core.android.Auth
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.CommitInfo
import com.dropbox.core.v2.files.DeletedMetadata
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.FolderMetadata
import com.dropbox.core.v2.files.ListFolderErrorException
import com.dropbox.core.v2.files.Metadata
import com.dropbox.core.v2.files.UploadSessionCursor
import com.dropbox.core.v2.files.UploadSessionFinishArg
import com.dropbox.core.v2.files.WriteMode
import org.antrack.app.DROPBOX_KEY
import org.antrack.app.cloud.CloudContentHashes
import org.antrack.app.cloud.CloudFileMetadata
import org.antrack.app.cloud.CloudMetadata
import org.antrack.app.cloud.ICloudProvider
import org.antrack.app.cloud.Status
import org.antrack.app.functions.className
import org.antrack.app.functions.logD
import org.antrack.app.functions.logE
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

@Suppress("BlockingMethodInNonBlockingContext")
class Dropbox(token: String = "") : ICloudProvider {
    lateinit var client: DbxClientV2

    private val config by lazy { DbxRequestConfig("AnTrack") }

    private val changeCursors = ConcurrentHashMap<String, String>()
    private val syncCursors = ConcurrentHashMap<String, String>()
    private val watchCursors = ConcurrentHashMap<String, String>()

    private val cachedFileList = ConcurrentHashMap<String, MutableList<CloudMetadata>>()
    private val needsFullResync = ConcurrentHashMap<String, Boolean>()
    private val changesCounter = ConcurrentHashMap<String, Int>()

    companion object {
        const val RESET_CACHE_AFTER_NUM_CHANGES = 100
        const val BATCH_SIZE = 1000
    }

    init {
        val credential = try {
            DbxCredential.Reader.readFully(token)
        } catch (_: Exception) {
            null
        }

        if (credential != null) {
            initDropbox(credential)
        } else {
            initDropboxOld(token)
        }
    }

    override fun hasSameContent(content: ByteArray, remote: CloudFileMetadata): Boolean {
        return CloudContentHashes.matchesDropbox(content, remote)
    }

    private fun initDropboxOld(token: String) {
        resetSessionState()
        d("Dropbox: init with legacy token")
        client = DbxClientV2(config, token)
    }

    private fun initDropbox(credential: DbxCredential) {
        resetSessionState()
        val dateFormatter = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.US)
        d("Dropbox: init, expires at: ${dateFormatter.format(credential.expiresAt)}")
        client = DbxClientV2(config, credential)
    }

    private fun resetSessionState() {
        changeCursors.clear()
        syncCursors.clear()
        watchCursors.clear()
        cachedFileList.clear()
        needsFullResync.clear()
        changesCounter.clear()
    }

    override fun auth(activity: Activity) {
        Auth.startOAuth2PKCE(activity, DROPBOX_KEY, config)
    }

    override fun resumeAuth(): String {
        val credential = Auth.getDbxCredential()
        return if (credential != null) {
            initDropbox(credential)
            credential.toString()
        } else {
            ""
        }
    }

    override fun getStatus(): Status {
        val status = try {
            Status(
                userId = client.users().currentAccount.accountId,
                email = client.users().currentAccount.email,
                isConnected = true,
            )
        } catch (_: InvalidAccessTokenException) {
            Status(
                isConnected = false,
                isTokenExpired = true,
            )
        } catch (e: Exception) {
            printError(e)
            Status(isConnected = false)
        }

        return status.copy(provider = "dropbox")
    }

    override fun putFile(
        iStream: InputStream,
        rFile: String,
    ): CloudFileMetadata {
        val meta = iStream.use { stream ->
            client.files()
                .uploadBuilder(rFile)
                .withMode(WriteMode.OVERWRITE)
                .uploadAndFinish(stream)
        }

        d("PutFile: $rFile rev=${meta.rev} size=${meta.size}")
        invalidateCachesForPath(rFile)
        return dropboxFileMetaToCloudMeta(meta)
    }

    override fun getFile(
        rFile: String,
        oStream: OutputStream,
    ): CloudFileMetadata {
        val meta = oStream.use { stream ->
            client.files()
                .downloadBuilder(rFile)
                .download(stream)
        }

        d("GetFile: $rFile rev=${meta.rev} size=${meta.size}")
        return dropboxFileMetaToCloudMeta(meta)
    }

    override fun putFile(
        lFile: String,
        rFile: String,
    ): CloudFileMetadata {
        return FileInputStream(File(lFile)).use { iStream ->
            putFile(iStream, rFile)
        }
    }

    override fun putFileBatch(
        files: List<Pair<ByteArray, String>>,
    ): Map<String, CloudFileMetadata> {
        if (files.isEmpty()) return emptyMap()

        if (files.size == 1) {
            val (content, path) = files.first()
            return mapOf(path to putFile(ByteArrayInputStream(content), path))
        }

        val results = mutableMapOf<String, CloudFileMetadata>()
        files.chunked(BATCH_SIZE).forEach { chunk ->
            results.putAll(uploadBatchChunk(chunk))
        }
        files.forEach { (_, path) -> invalidateCachesForPath(path) }
        return results
    }

    private fun uploadBatchChunk(
        files: List<Pair<ByteArray, String>>,
    ): Map<String, CloudFileMetadata> {
        val sessionArgs = files.map { (content, path) ->
            val sessionId = ByteArrayInputStream(content).use { stream ->
                client.files()
                    .uploadSessionStartBuilder()
                    .withClose(true)
                    .start()
                    .uploadAndFinish(stream)
                    .sessionId
            }

            UploadSessionFinishArg(
                UploadSessionCursor(sessionId, content.size.toLong()),
                CommitInfo.newBuilder(path)
                    .withMode(WriteMode.OVERWRITE)
                    .build(),
            )
        }

        val batchResult = client.files().uploadSessionFinishBatchV2(sessionArgs)
        val results = mutableMapOf<String, CloudFileMetadata>()

        batchResult.entries.forEachIndexed { index, entry ->
            when {
                entry.isSuccess -> {
                    val meta = entry.successValue
                    val path = files[index].second
                    d("PutFileBatch: $path rev=${meta.rev} size=${meta.size}")
                    results[path] = dropboxFileMetaToCloudMeta(meta)
                }
                entry.isFailure -> {
                    d("PutFileBatch failed for ${files[index].second}: ${entry.failureValue}")
                }
            }
        }

        return results
    }

    override fun getFile(
        rFile: String,
        lFile: String,
    ): CloudFileMetadata {
        return FileOutputStream(File(lFile)).use { oStream ->
            getFile(rFile, oStream)
        }
    }

    override fun deleteFile(
        rPath: String,
        permanent: Boolean,
    ) {
        if (permanent) {
            client.files().permanentlyDelete(rPath)
        } else {
            client.files().deleteV2(rPath)
        }
        invalidateCachesForPath(rPath)
    }

    override fun moveFile(
        rPathFrom: String,
        rPathTo: String,
    ): CloudMetadata {
        try {
            client.files().createFolderV2(rPathTo.parentDir())
        } catch (_: Exception) {
        }
        try {
            client.files().deleteV2(rPathTo)
        } catch (_: Exception) {
        }

        val result = client.files().moveV2(rPathFrom, rPathTo)
        invalidateCachesForPath(rPathFrom)
        invalidateCachesForPath(rPathTo)
        return dropboxMetaToCloudMeta(result.metadata)
    }

    private fun String.parentDir(): String {
        val index = lastIndexOf('/')
        return if (index <= 0) "/" else substring(0, index)
    }

    private fun invalidateCachesForPath(path: String) {
        val parent = path.parentDir()

        listOf(path, parent).forEach { cachedPath ->
            cachedFileList.remove(cachedPath)
            changeCursors.remove(cachedPath)
            syncCursors.remove(cachedPath)
            needsFullResync.remove(cachedPath)
        }

        // The watch cursor is an event-stream position, not listing cache state.
        // Clearing it here can race with long-poll and can also hide a command
        // uploaded through this provider before the watcher establishes a new cursor.
        needsFullResync[parent] = true
    }

    override fun getMetadata(rFile: String): CloudMetadata {
        val meta = client.files().getMetadata(rFile)
        return dropboxMetaToCloudMeta(meta)
    }

    override fun checkForChanges(rDir: String): Boolean {
        val startCursor = changeCursors[rDir]
        d("checkForChanges: dir=$rDir cursor=${startCursor?.take(8) ?: "null"}")

        val first = try {
            if (startCursor == null) {
                client.files().listFolder(rDir)
            } else {
                client.files().listFolderContinue(startCursor)
            }
        } catch (e: ListFolderErrorException) {
            if (e.isPathNotFound()) {
                d("checkForChanges: dir=$rDir not found, treating as no changes")
                changeCursors.remove(rDir)
                return false
            }
            printError(e)
            throw e
        } catch (e: Exception) {
            printError(e)
            throw e
        }

        var result = first
        var numChanges = 0
        var page = 1

        while (true) {
            numChanges += result.entries.size
            changeCursors[rDir] = result.cursor
            d(
                "checkForChanges: dir=$rDir page=$page " +
                    "entries=${result.entries.size} hasMore=${result.hasMore}",
            )
            if (!result.hasMore) break
            page++
            result = client.files().listFolderContinue(result.cursor)
        }

        if (numChanges > 0) {
            val newCount = changesCounter.getOrDefault(rDir, 0) + numChanges
            if (newCount >= RESET_CACHE_AFTER_NUM_CHANGES) {
                needsFullResync[rDir] = true
                changesCounter[rDir] = 0
            } else {
                changesCounter[rDir] = newCount
            }
        }

        d("checkForChanges: dir=$rDir totalChanges=$numChanges")
        return numChanges > 0
    }

    private fun ListFolderErrorException.isPathNotFound(): Boolean {
        return runCatching { errorValue?.pathValue?.isNotFound }.getOrNull() == true ||
            message?.contains("\"not_found\"") == true
    }

    override fun listDir(
        rDir: String,
        deleted: Boolean,
        dirs: Boolean,
    ): List<CloudMetadata> {
        if (deleted) {
            return listDirFull(rDir, dirs, includeDeleted = true, updateCache = false)
        }

        return if (needsFullResync.getOrDefault(rDir, false)) {
            val fullList = listDirFull(rDir, dirs)
            needsFullResync[rDir] = false
            changesCounter[rDir] = 0
            fullList
        } else if (cachedFileList.containsKey(rDir)) {
            try {
                listDirViaCache(rDir, dirs)
            } catch (e: Exception) {
                printError(e)
                listDirFull(rDir, dirs)
            }
        } else {
            listDirFull(rDir, dirs)
        }
    }

    private fun listDirViaCache(rDir: String, withDirs: Boolean): List<CloudMetadata> {
        val cachedList = cachedFileList[rDir] ?: CopyOnWriteArrayList<CloudMetadata>().also {
            cachedFileList[rDir] = it
        }

        val cursor = syncCursors[rDir]
        if (cursor != null) {
            val result = client.files().listFolderContinue(cursor)
            syncCursors[rDir] = result.cursor

            for (metadata in result.entries) {
                when (metadata) {
                    is FileMetadata -> {
                        cachedList.removeAll { it.path == metadata.pathDisplay }
                        cachedList.add(dropboxFileMetaToCloudMeta(metadata))
                    }
                    is FolderMetadata -> {
                        cachedList.removeAll { it.path == metadata.pathDisplay }
                        cachedList.add(dropboxFolderMetaToCloudMeta(metadata))
                    }
                    is DeletedMetadata -> {
                        cachedList.removeAll { it.path == metadata.pathDisplay }
                    }
                }
            }
        }

        return sortMetadataList(cachedList, withDirs)
    }

    private fun listDirFull(
        rDir: String,
        withDirs: Boolean,
        includeDeleted: Boolean = false,
        updateCache: Boolean = true,
    ): List<CloudMetadata> {
        var result = client.files()
            .listFolderBuilder(rDir)
            .withIncludeDeleted(includeDeleted)
            .start()

        val metadataList = mutableListOf<CloudMetadata>()

        while (true) {
            for (metadata in result.entries) {
                when (metadata) {
                    is FileMetadata -> metadataList.add(dropboxFileMetaToCloudMeta(metadata))
                    is FolderMetadata -> metadataList.add(dropboxFolderMetaToCloudMeta(metadata))
                }
            }
            if (!result.hasMore) break
            result = client.files().listFolderContinue(result.cursor)
        }

        if (updateCache) {
            cachedFileList[rDir] = CopyOnWriteArrayList(metadataList)
            syncCursors[rDir] = result.cursor
        }

        return sortMetadataList(metadataList, withDirs)
    }

    private fun sortMetadataList(
        list: List<CloudMetadata>,
        withDirs: Boolean,
    ): List<CloudMetadata> {
        return if (withDirs) {
            val dirs = list.filter { it !is CloudFileMetadata }.sortedBy { it.name }
            val files = list.filterIsInstance<CloudFileMetadata>().sortedBy { it.name }
            dirs + files
        } else {
            list.filterIsInstance<CloudFileMetadata>().sortedBy { it.name }
        }
    }

    override fun listDirRecursive(
        rDir: String,
        fileList: MutableList<CloudMetadata>,
    ) {
        val currentList = listDir(rDir, dirs = true)
        val toAdd = ArrayList<CloudMetadata>()

        for (meta in currentList) {
            if (meta !is CloudFileMetadata) {
                listDirRecursive(meta.path, fileList)
            }
            toAdd.add(meta)
        }
        fileList.addAll(toAdd)
    }

    override fun createDir(rDir: String): CloudMetadata {
        val result = client.files().createFolderV2(rDir)
        invalidateCachesForPath(rDir)
        return dropboxFolderMetaToCloudMeta(result.metadata)
    }

    override fun watchForChanges(dir: String): ArrayList<String>? {
        val fileList = ArrayList<String>()

        return try {
            val initialCursor = watchCursors[dir] ?: run {
                val newCursor = client.files()
                    .listFolderBuilder(dir)
                    .withRecursive(true)
                    .start()
                    .cursor
                watchCursors.putIfAbsent(dir, newCursor) ?: newCursor
            }

            val longpoll = client.files().listFolderLongpoll(initialCursor, 300)
            if (longpoll.changes) {
                var hasMore = true
                var cursor = initialCursor

                while (hasMore) {
                    val folderResult = client.files().listFolderContinue(cursor)
                    cursor = folderResult.cursor
                    watchCursors[dir] = cursor
                    hasMore = folderResult.hasMore

                    for (metadata in folderResult.entries) {
                        val path = metadata.pathDisplay ?: continue
                        if (metadata is FileMetadata) {
                            d("watchForChanges: modified file: $path")
                            fileList.add(path)
                        }
                    }
                }
            }

            fileList.ifEmpty { null }
        } catch (e: Exception) {
            printError(e)
            null
        }
    }

    private fun dropboxMetaToCloudMeta(meta: Metadata): CloudMetadata {
        return when (meta) {
            is FolderMetadata -> dropboxFolderMetaToCloudMeta(meta)
            is FileMetadata -> dropboxFileMetaToCloudMeta(meta)
            else -> error("Unsupported metadata type: ${meta::class.java}")
        }
    }

    private fun dropboxFileMetaToCloudMeta(meta: FileMetadata): CloudFileMetadata {
        return CloudFileMetadata(
            path = meta.pathDisplay ?: meta.name,
            name = meta.name,
            lastModified = meta.serverModified.time,
            size = meta.size,
            hash = meta.contentHash ?: "",
            revision = meta.rev,
        )
    }

    private fun dropboxFolderMetaToCloudMeta(meta: FolderMetadata): CloudMetadata {
        return CloudMetadata(
            path = meta.pathDisplay ?: meta.name,
            name = meta.name,
        )
    }

    private fun d(message: String) {
        logD(className, message)
    }

    private fun printError(e: Exception) {
        logE(className, "Error: ${e.message}")
    }
}
