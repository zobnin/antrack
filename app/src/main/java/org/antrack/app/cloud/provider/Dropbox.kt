package org.antrack.app.cloud.provider

import android.app.Activity
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.InvalidAccessTokenException
import com.dropbox.core.android.Auth
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.*
import org.antrack.app.DROPBOX_KEY
import org.antrack.app.cloud.CloudFileMetadata
import org.antrack.app.cloud.CloudMetadata
import org.antrack.app.cloud.ICloudProvider
import org.antrack.app.cloud.Status
import org.antrack.app.functions.className
import org.antrack.app.functions.logD
import org.antrack.app.functions.logE
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

@Suppress("BlockingMethodInNonBlockingContext")
class Dropbox(token: String = "") : ICloudProvider {
    lateinit var client: DbxClientV2

    private val config by lazy { DbxRequestConfig("AnTrack") }
    private var checkCursors = hashMapOf<String, String>()
    private var watchCursors = hashMapOf<String, String>()

    init {
        val credential = getCredential(token)
        initDropbox(credential, token)
    }

    private fun getCredential(token: String): DbxCredential? {
        return try {
            DbxCredential.Reader.readFully(token)
        } catch (e: Exception) {
            null
        }
    }

    private fun initDropbox(credential: DbxCredential?, token: String) {
        if (credential != null) {
            initDropbox(credential)
        } else {
            initDropboxOld(token)
        }
    }

    private fun initDropbox(credential: DbxCredential) {
        val dateFormatter = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.US)
        val expiresAtStr = dateFormatter.format(credential.expiresAt)

        d("Init dropbox")
        d("Dropbox token expires at: $expiresAtStr")

        client = DbxClientV2(config, credential)
    }

    private fun initDropboxOld(token: String) {
        d("Dropbox token: $token")

        client = DbxClientV2(config, token)
    }

    override fun auth(activity: Activity) {
        Auth.startOAuth2PKCE(activity, DROPBOX_KEY, config)
    }

    override fun resumeAuth(): String {
        val credential = Auth.getDbxCredential()

        if (credential != null) {
            initDropbox(credential)
            return credential.toString()
        } else {
            return ""
        }
    }

    override fun getStatus(): Status {
        val status = try {
            Status(
                userId = client.users().currentAccount.accountId,
                email = client.users().currentAccount.email,
                isConnected = true,
            )
        } catch (e: InvalidAccessTokenException) {
            Status(
                isConnected = false,
                isTokenExpired = true,
            )
        } catch (e: Exception) {
            printError(e)
            Status(
                isConnected = false,
            )
        }

        return status.copy(provider = "dropbox")
    }

    override fun putFile(
        iStream: InputStream,
        rFile: String,
    ): CloudFileMetadata {

        val meta = client.files()
            .uploadBuilder(rFile)
            .withMode(WriteMode.OVERWRITE)
            .uploadAndFinish(iStream)

        iStream.close()

        d("PutFile: $rFile $meta")

        return dropboxFileMetaToCloudMeta(meta)
    }

    override fun getFile(
        rFile: String,
        oStream: OutputStream,
    ): CloudFileMetadata {

        val meta = client.files()
            .downloadBuilder(rFile)
            .download(oStream)

        oStream.close()

        d("GetFile: $rFile $meta")

        return dropboxFileMetaToCloudMeta(meta)
    }

    override fun putFile(
        lFile: String,
        rFile: String,
    ): CloudFileMetadata {

        val file = File(lFile)
        val iStream = FileInputStream(file)

        return putFile(iStream, rFile)
    }

    override fun getFile(
        rFile: String,
        lFile: String,
    ): CloudFileMetadata {

        val file = File(lFile)
        val os = FileOutputStream(file)

        return getFile(rFile, os)
    }

    override fun deleteFile(
        rPath: String,
        permanent: Boolean,
    ) {
        if (permanent) {
            client.files().permanentlyDelete(rPath)
        } else {
            client.files().delete(rPath)
        }
    }

    override fun moveFile(
        rPathFrom: String,
        rPathTo: String
    ): CloudMetadata {

        createFolder(rPathTo)

        val result = client.files().moveV2(rPathFrom, rPathTo)

        return dropboxMetaToCloudMeta(result.metadata)
    }

    private fun createFolder(rPathTo: String) {
        try {
            client.files().createFolderV2(rPathTo.getBaseDir())
        } catch (e: Exception) {
            //
        }
    }

    private fun String.getBaseDir(): String {
        return if (contains("/")) {
            substring(0, lastIndexOf('/'))
        } else {
            this
        }
    }

    override fun getMetadata(
        rFile: String,
    ): CloudMetadata {

        val meta = client.files().getMetadata(rFile)

        return dropboxMetaToCloudMeta(meta)
    }

    override fun listDir(
        rDir: String,
        deleted: Boolean,
        dirs: Boolean,
    ): List<CloudMetadata> {

        val metadataList = mutableListOf<CloudMetadata>()

        var result = client.files()
            .listFolderBuilder(rDir)
            .withIncludeDeleted(deleted)
            .start()

        while (true) {
            for (metadata in result.entries) {
                when (metadata) {
                    is FileMetadata -> metadataList.add(dropboxFileMetaToCloudMeta(metadata))
                    is FolderMetadata -> metadataList.add(dropboxFolderMetaToCloudMeta(metadata))
                }
            }

            if (!result.hasMore) {
                break
            }

            result = client.files().listFolderContinue(result.cursor)
        }

        return if (dirs) {
            val dirs = metadataList
                .filterNot { it is CloudFileMetadata }
                .sortedBy { it.name }

            val files = metadataList
                .filterIsInstance<CloudFileMetadata>()
                .sortedBy { it.name }

            dirs + files
        } else {
            metadataList
                .filterIsInstance<CloudFileMetadata>()
                .sortedBy { it.name }
        }
    }

    override fun listDirRecursive(
        rDir: String,
        fileList: MutableList<CloudMetadata>,
    ) {

        val fileList1 = listDir(rDir)
        val fileList2 = ArrayList<CloudMetadata>()

        for (meta in fileList1) {
            if (meta.path.endsWith("/")) {
                listDirRecursive(meta.path, fileList)
            }
            fileList2.add(meta)
        }

        fileList.addAll(fileList2)
    }

    override fun createDir(
        rDir: String,
    ): CloudMetadata {

        val result = client.files().createFolderV2(rDir)
        return dropboxFolderMetaToCloudMeta(result.metadata)
    }

    override fun checkForChanges(
        rDir: String
    ): Boolean {

        val result = listFolder(rDir)

        checkCursors[rDir] = result.cursor

        return result.entries.isNotEmpty()
    }

    private fun listFolder(rDir: String): ListFolderResult {
        return if (checkCursors[rDir] == null) {
            client.files().listFolder(rDir)
        } else {
            client.files().listFolderContinue(checkCursors[rDir])
        }
    }

    override fun watchForChanges(
        dir: String
    ): ArrayList<String>? {

        val longpollResult: ListFolderLongpollResult
        var folderResult: ListFolderResult

        var fileList: ArrayList<String>? = null

        try {
            if (!watchCursors.containsKey(dir)) {
                folderResult = client.files().listFolderBuilder(dir).withRecursive(true).start()
                watchCursors[dir] = folderResult.cursor
            }

            longpollResult = client.files().listFolderLongpoll(watchCursors[dir], 300)

            if (longpollResult.changes) {
                var hasMore = true
                while (hasMore) {
                    folderResult = client.files().listFolderContinue(watchCursors[dir])
                    watchCursors[dir] = folderResult.cursor
                    hasMore = folderResult.hasMore

                    fileList = ArrayList()
                    for (md in folderResult.entries) {
                        val changedFilePath = md.pathLower

                        if (md is DeletedMetadata || md is FolderMetadata)
                            continue

                        d("watchForChanges: modified file: $changedFilePath")
                        fileList.add(changedFilePath)
                    }
                }
            }
            return fileList
        } catch (e: Exception) {
            printError(e)
            return null
        }
    }

    private fun dropboxMetaToCloudMeta(meta: Metadata): CloudMetadata {
        return when (meta) {
            is FolderMetadata -> dropboxFolderMetaToCloudMeta(meta)
            else -> dropboxFileMetaToCloudMeta(meta as FileMetadata)
        }
    }

    private fun dropboxFileMetaToCloudMeta(meta: FileMetadata): CloudFileMetadata {
        return CloudFileMetadata(
            path = meta.pathLower,
            name = meta.name,
            lastModified = meta.serverModified.time,
            size = meta.size,
            hash = meta.contentHash,
            revision = meta.rev,
        )
    }

    private fun dropboxFolderMetaToCloudMeta(meta: FolderMetadata): CloudMetadata {
        return CloudMetadata(
            path = meta.pathLower,
            name = meta.name,
        )
    }

    private fun d(message: String) {
        logD(className, message)
    }

    private fun printError(e: Exception) {
        logE(className, "Error: " + e.message)
    }
}
