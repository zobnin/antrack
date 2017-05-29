package org.antrack.app

import android.app.Activity
import org.antrack.app.libs.L
import org.antrack.app.libs.Net
import org.antrack.app.plugins.Dropbox
import java.io.File
import java.io.InterruptedIOException
import java.util.*

// Pw - Plugin wrapper

object Pw {

    private val TAG = "Pw"
    // Max sleep time if no connection to cloud
    private val MAX_SLEEP = 320

    private var dPlugin: Dropbox? = null

    private var connected = false

    init {
        connect()
    }

    // на самом деле объект dPlugin будет успешно создан и без интернета
    // но проверка на коннект позволяет избежать всяких ситуаций с NullException
    fun connect(): Boolean {
        if (connected) {
            return true
        }

        val token = Settings.readToken()
        if (token.isNullOrEmpty()) {
            return false
        }

        if ("dropbox" == Settings[C.S_PLUGIN]) {
            dPlugin = Dropbox(token!!)
            connected = true
            L.d(TAG, "Connected to cloud")
            return true
        }

        return false
    }

    val isConnected: Boolean
        get() {
            if (connected) {
                return true
            } else {
                L.e(TAG, "No connection to cloud")
                return false
            }
        }

    @Throws(InterruptedException::class)
    fun auth(activity: Activity) {
        if (Settings["plugin"] == "dropbox") {
            dPlugin = Dropbox()
            dPlugin!!.auth(activity)
        }
    }

    fun resume(): String? {
        if (Settings["plugin"] == "dropbox") {
            return dPlugin!!.resume()
        } else {
            return null
        }
    }

    val email: String?
        get() {
            if (!connect()) return null

            if (Settings["plugin"] == "dropbox") {
                return dPlugin!!.email
            } else {
                return null
            }
        }

    @Throws(InterruptedException::class)
    fun putFile(lFile: String, rFile: String, delete: Boolean) {
        if (!connect()) return

        if (Settings["plugin"] == "dropbox") {
            L.d(TAG, "Put file $lFile as $rFile")
            dPlugin!!.putFile(lFile, rFile, delete)
        }
    }

    @Throws(InterruptedException::class)
    fun getFile(lFile: String, rFile: String) {
        if (!connect()) return

        if (Settings["plugin"] == "dropbox") {
            L.d(TAG, "Get file $rFile as $lFile")
            dPlugin!!.getFile(lFile, rFile)
        }
    }

    @Throws(InterruptedException::class)
    fun delete(rFile: String, permanent: Boolean) {
        if (!connect()) return

        if (Settings["plugin"] == "dropbox") {
            dPlugin!!.delete(rFile, permanent)
        }
    }

    @Throws(InterruptedException::class)
    fun listDir(rDir: String): ArrayList<String>? {
        if (!connect()) return null

        if (Settings["plugin"] == "dropbox") {
            L.d(TAG, rDir)
            return dPlugin!!.listDir(rDir)
        }
        return null
    }

    @Throws(InterruptedException::class)
    fun listDir(rDir: String, withDeleted: Boolean): ArrayList<String>? {
        if (!connect()) return null

        if (Settings["plugin"] == "dropbox") {
            L.d(TAG, rDir)
            return dPlugin!!.listDir(rDir, withDeleted)
        }
        return null
    }

    @Throws(InterruptedException::class)
    fun getDir(lDir: String, rDir: String) {
        if (!connect()) return

        if (Settings["plugin"] == "dropbox") {
            L.d(TAG, "Get files from dir $rDir to dir $lDir")

            // We don't want to trigger fileObserver on every downloaded file
            // so we save dir to main folder and then move to devices folder
            val tempDir = Init.APP_DIR + "/" + File(rDir).name
            File(tempDir).mkdirs()
            dPlugin!!.getDir(tempDir, rDir)
            File(tempDir).renameTo(File(lDir))
        }
    }

    @Throws(InterruptedIOException::class)
    fun listDirs(rDir: String): ArrayList<String>? {
        if (!connect()) return null

        if (Settings["plugin"] == "dropbox") {
            L.d(TAG, "List dirs " + rDir)
            return dPlugin!!.listDirs(rDir)
        }
        return null
    }

    @Throws(InterruptedException::class)
    fun watchForChanges(dir: String): ArrayList<String>? {
        if (!connect()) return null

        if (Settings["plugin"] == "dropbox") {
            L.d(TAG, "Start watching")
            return dPlugin!!.watchForChanges(dir)
        }
        return null
    }

    @Synchronized @Throws(InterruptedException::class)
    fun waitOnline() {
        var i = 10
        while (!Net.isOnline) {
            L.d(TAG, "No internet, sleep $i seconds")
            Thread.sleep((i * 1000).toLong())
            if (i < MAX_SLEEP)
                i *= 2
        }
    }

}
