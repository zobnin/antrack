package org.antrack.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Base64
import app.R
import com.splunk.mint.Mint
import org.antrack.app.*
import org.antrack.app.libs.Crypto
import org.antrack.app.libs.L
import org.antrack.app.libs.Utils
import org.antrack.app.libs.WakeLocks
import java.io.IOException

class MainService : Service() {
    internal val TAG = "MainService"
    lateinit var context: Context
    private var initDone = false

    override fun onCreate() {
        super.onCreate()
        context = this
        init()
    }

    private fun init() {
        if (initDone) {
            return
        }

        // Error reporting
        Mint.initAndStartSession(this@MainService, "8af105a4")

        setAlarm()

        // File watcher must be started AFTER creating all catalogs
        // Catalogs for modules created on load step
        CC.runModules("load", "")

        startFileWatcher()

        // Wait for file watcher
        // FIXME
        Utils.sleep(1)

        /* Write device features */

        val feat = Features()
        feat.write(this@MainService, Init.MAIN_DIR + C.FEATURES_FILE)

        /* Write OneSignal Id and generate key pair */

        OSignal.writeId()
        Keys.saveKeys()

        /* Bootstrap */

        // Generate /modules file
        CC.parseCommand("!modules")
        CC.parseBootstrap()

        /* Unpack assets */

        Utils.unpackAsset(context, C.ALARM_ASSET)

        /* Get and watch for clt / ctlq */

        startCtlWatcher()

        Logger.started()

        initDone = true
    }

    private fun setAlarm() {
        val updateInterval = Settings[C.S_UPDATE_INTERVAL]
        val time = java.lang.Long.parseLong(updateInterval) * 60 * 1000
        Alarm.set(time)
    }

    private fun startFileWatcher() {
        FileWatcher.addCallback("service", LocalFileUpdated())
    }

    private fun stopFileWatcher() {
        FileWatcher.removeCallback("service")
    }

    private fun startCtlWatcher() {
        val ctlEnabled = Settings[C.S_ENABLE_CTL]
        if (ctlEnabled == C.TRUE) {
            try {
                U.getFile(C.CONTROL_Q_FILE)
                CloudWatcher.addCallback("service", CloudFileUpdated())
            } catch (e: Exception) {
                L.d(TAG, "Can't parse ctlq file: " + e)
            }

        }
    }

    private fun stopCtlWatcher() {
        val ctlEnabled = Settings[C.S_ENABLE_CTL]
        if (ctlEnabled == C.TRUE) {
            CloudWatcher.removeCallback("service")
        }
    }

    // Callback waits for local file changes
    private inner class LocalFileUpdated : FileWatcher.Callback {
        override fun onFileUpdate(path: String) {
            if (path.isEmpty()) {
                return
            }

            // Current device ctl changed -> read and execute command
            if (path.endsWith(C.CONTROL_FILE)) {
                try {
                    U.parseCtl()
                } catch (e: IOException) {
                    L.e(TAG, "Can't read ctl file: " + e.toString())
                }

            } else if (path.endsWith(C.CONTROL_Q_FILE)) {
                val ctlEnabled = Settings[C.S_ENABLE_CTL]
                if (C.TRUE == ctlEnabled) {
                    try {
                        U.parseCtlq()
                    } catch (e: IOException) {
                        L.e(TAG, "Can't read ctlq file: " + e.toString())
                    }

                }
            } else {
                try {
                    val isUploaderEnabled = Settings[C.S_ENABLE_UPLOADER]
                    if (C.TRUE == isUploaderEnabled) {
                        U.uploadFile(path)
                    }
                } catch (e: InterruptedException) {
                    L.e(TAG, "processFile interrupted: " + e.toString())
                }

            }// Other file changed -> upload to cloud
            // Current device ctlq changed -> read and execute commands
        }

        override val watchFile: String?
            get() = "/" + Init.DEVICE_NAME_IMEI + "/"
    }

    // Callback waits for ctl & ctlq changes
    private inner class CloudFileUpdated : CloudWatcher.Callback {
        override fun onFileUpdate(path: String) {
            try {
                if (Pw.isConnected)
                    Pw.getFile(Init.DEVICES_DIR + path, path)
            } catch (e: Exception) {
                L.e(TAG, "CloudFileUpdated exception: " + e)
            }

        }

        override val watchFile: String?
            get() = "/" + Init.DEVICE_NAME_IMEI + C.CONTROL_FILE
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
            val wl = WakeLocks(this@MainService, "ServiceWakelock")
            wl.lock()

            init()

            if (intent != null) {

                intent.action.let {
                    L.d(TAG, "onStartCommand get: " + intent.action)
                }

                when (intent.action) {
                    C.ACTION_BOOT -> CC.runModules(C.ACTION_BOOT, "")
                    C.ACTION_ALARM -> {
                        Logger.alarm()
                        CC.runModules(C.ACTION_ALARM, "")
                    }
                    C.ACTION_SCREENON -> CC.runModules(C.ACTION_SCREENON, "")
                    C.ACTION_OUTGOINGCALL -> {
                        val outNumber = intent.getStringExtra("phoneNumber")
                        if (outNumber != null)
                            CC.runModules(C.ACTION_OUTGOINGCALL, outNumber)
                    }
                    C.ACTION_INCOMINGCALL -> {
                        val number = intent.getStringExtra("phoneNumber")
                        if (number != null)
                            CC.runModules(C.ACTION_INCOMINGCALL, number)
                    }
                    C.ACTION_COMMAND -> CC.parseCommand(intent.getStringExtra("command"))
                    C.ACTION_CTL_ENABLED -> startCtlWatcher()
                    C.ACTION_CTL_DISABLED -> stopCtlWatcher()
                    C.ACTION_PUSH -> {
                        val device = intent.getStringExtra("device")
                        val message = intent.getStringExtra("message")
                        processPush(device, message)
                    }
                    C.ACTION_AUTH_DEVICE -> {
                        Notify.cancel()
                        val device2 = intent.getStringExtra("device")
                        if (TrustedDevices.trust(device2)) {
                            Logger.trusted(device2)
                            val message2 = intent.getStringExtra("message")
                            processPush(device2, message2)
                        }
                    }
                    C.ACTION_BAN_DEVICE -> {
                        Notify.cancel()
                        TrustedDevices.ban(intent.getStringExtra("device"))
                        Logger.banned(intent.getStringExtra("device"))
                    }
                }
            }
            // Wait other threads
            Utils.sleep(5)
            wl.unlock()
        }.start()

        return Service.START_STICKY
    }

    private fun processPush(remoteDeviceName: String, remoteEncMessage: String) {
        val key = TrustedDevices.getKey(remoteDeviceName)

        if (key.isEmpty()) {
            showNotify(remoteDeviceName)
            return
        } else if (key == "banned") {
            L.d(TAG, "Device $remoteDeviceName banned")
            return
        }

        val decrypted: String

        try {
            decrypted = Crypto.decryptStringRSA(
                    Base64.decode(remoteEncMessage, Base64.DEFAULT),
                    Crypto.stringToPublicKey(key.trim { it <= ' ' }))
        } catch (e: Exception) {
            Logger.cantDecrypt(remoteDeviceName)
            L.e(TAG, "Can't decrypt message: " + e.toString())
            return
        }

        // Message format: command::<command>::<salt>
        val decryptedA = decrypted.split("::".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        // Decryption with wrong key?
        if ("command" != decryptedA[0]) {
            showNotify(remoteDeviceName)
            return
        }

        val cmd = decryptedA[1]

        Logger.getPush(remoteDeviceName, cmd)
        L.d(TAG, "Push command: $cmd, from device: $remoteDeviceName")

        CC.parseCommand(cmd)
    }

    private fun showNotify(remoteDeviceName: String) {
        Notify.show(applicationContext,
                resources.getString(R.string.auth_request),
                resources.getString(R.string.allow) + " " +
                        remoteDeviceName + " " +
                        resources.getString(R.string.to_control_this_device), remoteDeviceName)
    }

    override fun onDestroy() {
        super.onDestroy()

        stopFileWatcher()
        stopCtlWatcher()

        val enabled = Settings[C.S_ENABLE_SERVICE]
        if (enabled == "false") {
            Alarm.cancel(this)
        }

        Logger.stopped()

        stopSelf()
    }

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }
}
