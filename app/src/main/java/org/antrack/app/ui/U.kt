package org.antrack.app.ui

import android.content.Intent
import android.util.Base64
import org.antrack.app.*
import org.antrack.app.libs.Crypto
import org.antrack.app.libs.Files
import org.antrack.app.libs.L
import org.antrack.app.libs.SessionIdGenerator
import org.antrack.app.service.MainService
import java.io.File
import java.io.IOException
import java.util.*

object U {
    private val TAG = "U"

    // Get full dir name with main dir and current device name
    fun getLocalPath(path: String): String {
        return Init.DEVICES_DIR + State.device.dir + path
    }

    // Get full path in cloud
    fun getCloudPath(path: String): String {
        return "/" + State.device.dir + path
    }

    // Download file for current device
    fun getFile(file: String) {
        try {
            Files.mkdirsForFile(getLocalPath(file))

            if (Pw.isConnected)
                Pw.getFile(getLocalPath(file), getCloudPath(file))
        } catch (e: Exception) {
            L.e(TAG, "Can't get file $file:$e")
        }

    }

    fun getFileAsync(file: String) {
        Thread(Runnable { getFile(file) }).start()
    }

    // Download all files in dir for current device
    fun getDir(dir: String) {
        try {
            if (Pw.isConnected)
                Pw.getDir(getLocalPath(dir), getCloudPath(dir))
        } catch (e: Exception) {
            L.e(TAG, "Can't get dir $dir: $e")
        }

    }

    // Upload file for current device
    fun putFile(file: String) {
        try {
            if (Pw.isConnected)
                Pw.putFile(getLocalPath(file), getCloudPath(file), false)
        } catch (e: Exception) {
            L.e(TAG, "Can't put file $file: $e")
        }

    }

    fun putFileAsync(file: String) {
        Thread(Runnable { putFile(file) }).start()
    }

    @Volatile private var listDirResult: ArrayList<String>? = null

    // List dir (arg: full path, return: full path)
    fun listDir(dir: String): ArrayList<String>? {
        val thread = Thread(Runnable {
            try {
                if (Pw.isConnected)
                    listDirResult = Pw.listDir(dir)
            } catch (e: Exception) {
                L.e(TAG, "Can't list dir $dir: $e")
            }
        })

        thread.start()
        try {
            thread.join()
        } catch (e: InterruptedException) {
            L.e(TAG, "listDir exception: " + e)
            return null
        }

        return listDirResult
    }

    // Compare cloud and local dir for current device
    fun compareDirs(dir: String): ArrayList<String>? {
        val remoteDir: ArrayList<String>? = listDir("/" + State.device.dir + dir)
        val localDir: ArrayList<String> = ArrayList(Arrays.asList(*File(getLocalPath(dir)).list()))

        if (remoteDir == null) return null

        val notInLocal: ArrayList<String> = remoteDir
                .map { File(it).name }
                .filterNotTo(ArrayList()) { localDir.contains(it) }

        return notInLocal
    }

    fun runCommandAsync(cmd: String) {
        if (State.device.isMain) {
            val ctlFile = getLocalPath(C.CONTROL_FILE)

            try {
                Files.writeTextFile(ctlFile, "!$cmd")
            } catch (e: IOException) {
                L.e(TAG, "Can't run command $cmd: $e")
            }

        } else {
            /*
            String ctlFile = getLocalPath(C.CONTROL_Q_FILE);

            try {
                Files.addLineToStack(ctlFile,
                        Utils.date(C.LAST_CMD_TIME_FORMAT) + " " + cmd, C.CONTROL_Q_MAX_LENGTH);
                putFileAsync(C.CONTROL_Q_FILE);

                OSignal.push(State.device.getOSId().trim(), cmd);
            } catch (IOException e) {
                L.e(TAG, "Can't run command " + cmd + ": " + e);
            }
            */
            try {
                // Message format: command::<command>::<salt>

                val idGenerator = SessionIdGenerator()
                val message = "command::" + cmd + "::" + idGenerator.nextSessionId()
                val encrypted = Crypto.encryptStringRSA(message, Keys.getPrivateKey())

                OSignal.push(State.device!!.osId.trim(),
                        Init.DEVICE_NAME_IMEI + " " +
                                Base64.encodeToString(encrypted, Base64.DEFAULT))
            } catch (e: Exception) {
                L.e(TAG, "Can't encrypt command: " + e.toString())
            }

        }
    }

    fun banDevice(deviceName: String) {
        val intent = Intent(App.context, MainService::class.java)
        intent.action = C.ACTION_PUSH
        intent.putExtra("device", deviceName)
        App.context!!.startService(intent)
    }
}
