package org.antrack.app.plugins

import android.app.Activity
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.*
import org.antrack.app.C
import org.antrack.app.libs.L
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

class Dropbox {
    private val TAG = "Dropbox"

    private var cursors: HashMap<String, String>? = null
    private lateinit var client: DbxClientV2

    constructor() {}

    constructor(token: String) {
        val config = DbxRequestConfig("AnTrack/1.0")
        client = DbxClientV2(config, token)
    }

    fun auth(activity: Activity) {
        Auth.startOAuth2Authentication(activity, C.DROPBOX_KEY)
    }

    fun resume(): String {
        return Auth.getOAuth2Token()
    }

    val email: String?
        get() {
            try {
                return client.users().currentAccount.email
            } catch (e: Exception) {
                L.e(TAG, "getEmail exception: " + e.toString())
                return null
            }

        }

    fun putFile(lFile: String, rFile: String, delete: Boolean) {
        val file = File(lFile)
        try {
            val `is` = FileInputStream(file)
            val meta = client.files().uploadBuilder(rFile)
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(`is`)
            `is`.close()
            L.d(TAG, "PutFile: " + rFile + " " + meta.toString())
            if (delete) {

                file.delete()
            }
        } catch (e: Exception) {
            L.e(TAG, "PutFile exception: " + e.toString())
        }

    }

    fun getFile(lFile: String, rFile: String) {
        val file = File(lFile)
        try {
            val os = FileOutputStream(file)
            val meta = client.files().downloadBuilder(rFile).download(os)
            os.close()
            L.d(TAG, "GetFile: " + rFile + " " + meta.toString())
        } catch (e: Exception) {
            L.e(TAG, "GetFile exception: " + e.toString())
        }

    }

    fun delete(path: String, permanent: Boolean) {
        try {
            if (permanent)
                client.files().permanentlyDelete(path)
            else
                client.files().delete(path)
        } catch (e: Exception) {
            L.e(TAG, "Delete exception: " + e.toString())
        }

    }

    @JvmOverloads fun listDir(rDir: String, withDeleted: Boolean = false): ArrayList<String>? {
        val fileList = ArrayList<String>()

        try {
            var result = client.files().listFolderBuilder(rDir)
                    .withIncludeDeleted(withDeleted)
                    .start()
            while (true) {
                for (metadata in result.entries) {
                    val path = metadata.pathLower
                    if (metadata is FolderMetadata)
                        fileList.add(path + "/")
                    else
                        fileList.add(path)
                }

                if (!result.hasMore) {
                    break
                }

                result = client.files().listFolderContinue(result.cursor)
            }
        } catch (e: Exception) {
            L.e(TAG, "listDir exception: " + e.toString())
            return null
        }

        return fileList
    }


    private fun listDirRecursive(rDir: String, fileList: ArrayList<String>) {
        val fileList1 = listDir(rDir)
        val fileList2 = ArrayList<String>()
        for (file in fileList1!!) {
            if (file.endsWith("/"))
                listDirRecursive(file, fileList)
            fileList2.add(file)
        }
        fileList.addAll(fileList2)
    }

    // List dirs and sub dirs
    fun listDirs(rDir: String): ArrayList<String> {
        val fileList = ArrayList<String>()
        listDirRecursive(rDir, fileList)
        return fileList
    }

    // Get all files from dir
    fun getDir(lDir: String, rDir: String) {
        val files = listDir(rDir)
        if (files == null) {
            L.e(TAG, "GetFiles: dir is empty")
            return
        }

        for (file in files) {
            getFile(lDir + "/" + File(file).name, file)
        }
    }

    fun watchForChanges(dir: String): ArrayList<String>? {
        val longpollResult: ListFolderLongpollResult
        var folderResult: ListFolderResult

        var fileList: ArrayList<String>? = null

        if (cursors == null)
            cursors = HashMap<String, String>()

        try {
            if (!cursors!!.containsKey(dir)) {
                folderResult = client.files().listFolderBuilder(dir).withRecursive(true).start()
                cursors!!.put(dir, folderResult.cursor)
            }

            longpollResult = client.files().listFolderLongpoll(cursors!![dir], C.DB_LONGPOLL_TIMEOUT.toLong())

            if (longpollResult.changes) {
                var hasMore = true
                while (hasMore) {
                    folderResult = client.files().listFolderContinue(cursors!![dir])
                    cursors!!.put(dir, folderResult.cursor)
                    hasMore = folderResult.hasMore

                    fileList = ArrayList<String>()
                    for (md in folderResult.entries) {
                        val changedFilePath = md.pathLower

                        if (md is DeletedMetadata || md is FolderMetadata)
                            continue

                        L.d(TAG, "watchForChanges: modified file: " + changedFilePath)
                        fileList.add(changedFilePath)
                    }
                }
            }
            return fileList
        } catch (e: Exception) {
            L.e(TAG, "watchForChanges exception: " + e.toString())
            return null
        }

    }
    /*
    private String longpollURL="https://notify.dropboxapi.com/2/files/list_folder/longpoll";

    private boolean longpoll(String cursor) {
        try {
            String charset = "UTF-8";

            URL url = new URL(longpollURL);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setConnectTimeout(C.DB_LONGPOLL_TIMEOUT * 1000);
            conn.setReadTimeout(C.DB_LONGPOLL_TIMEOUT * 1000);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestProperty("Accept-Charset", charset);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            String query =
                    "{\"cursor\": \"" + cursor +
                    "\",\"timeout\": " + Integer.toString(C.DB_LONGPOLL_TIMEOUT) + "}";

            L.e(TAG, "query: " + query);

            OutputStream os = conn.getOutputStream();
            os.write(query.getBytes());
            os.flush();

            int status = conn.getResponseCode();

            InputStream is;

            if(status >= HttpStatus.SC_BAD_REQUEST)
                is = conn.getErrorStream();
            else
                is = conn.getInputStream();

            String response = Utils.StreamToString(is);

            L.e(TAG, "Longpoll result: " + response);
            return response.contains("true");
        } catch (Exception e) {
            L.e(TAG, "Longpoll exception: " + e);
            return false;
        }
    }
*/
}// Return list of files in directory with full path
