package org.antrack.app.service

import android.annotation.TargetApi
import android.app.ActivityManager
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import org.antrack.app.*
import org.antrack.app.cloud.Cloud
import org.antrack.app.functions.className
import org.antrack.app.functions.logD
import org.antrack.app.functions.logE
import org.antrack.app.functions.wakelock
import org.antrack.app.receivers.Receivers
import org.antrack.app.service.watcher.CloudCtlChangeCallback
import org.antrack.app.service.watcher.LocalCtlChangeCallback
import org.antrack.app.service.watcher.UploaderCallback
import org.antrack.app.watcher.CloudWatcher
import org.antrack.app.watcher.FileWatcher
import unpackAsset

class CloudService : Service() {
    private val receivers by lazy { Receivers() }
    private val cc by lazy { CommandRunner() }
    private val intentActionProcessor by lazy { IntentActionProcessor(cc) }
    private var initDone = false

    companion object {
        fun isWorking(context: Context): Boolean {
            val manager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if (CloudService::class.java.name.equals(service.service.className)) {
                    return true
                }
            }
            return false
        }

        fun start(context: Context, action: String = "") {
            if (Settings.isServiceEnabled) {
                val serviceIntent = Intent(context, CloudService::class.java)
                if (action.isNotEmpty()) {
                    serviceIntent.action = action
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }

        fun stop(context: Context) {
            val serviceIntent = Intent(context, CloudService::class.java)
            context.stopService(serviceIntent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        init1()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1111, createNotification().build())
        }

        Env.executor.submit {
            wakelock {
                init1()
                processAction(intent)
            }
        }

        return START_STICKY
    }

    private fun init1() {
        try {
            if (initDone) return

            logD(className, "Service started")

            setAlarm()

            // File watcher must be started AFTER creating all catalogs
            // Catalogs for modules created on load step
            cc.executeModules("load", "")

            startFileWatcher()
            FileWatcher.waitForFile("init", "/${Env.deviceNameId}/") {
                init2()
            }
        } catch (e: Exception) {
            logE(className, "Init #1 error: ${e.message}")
        }
    }

    private fun init2() {
        try {
            /* Write device features */
            Features().write(Env.featuresFilePath)

            /* Bootstrap */
            cc.executeCommand("!modules")
            cc.executeBootstrap()

            /* Unpack assets */
            unpackAsset(ALARM_ASSET)

            /* Register receivers */
            receivers.registerPersistentReceivers()

            /* Get and watch for clt / ctlq */
            getCtlqFile()
            startCtlWatcher()

            initDone = true
        } catch (e: Exception) {
            logE(className, "Init #2 error: ${e.message}")
        }
    }

    private fun processAction(intent: Intent?) {
        if (intent?.action != null) {
            logD(className, "onStartCommand get: " + intent.action)
            intentActionProcessor.process(intent)
        }
    }

    private fun setAlarm() {
        val time = Settings.updateInterval * 60 * 1000
        Alarm.set(time)
    }

    private fun startFileWatcher() {
        FileWatcher.addCallback("service_ctl_watcher", LocalCtlChangeCallback(cc))
        FileWatcher.addCallback("service_uploader", UploaderCallback())
    }

    private fun stopFileWatcher() {
        FileWatcher.removeCallback("service_ctl_watcher")
        FileWatcher.removeCallback("service_uploader")
    }

    private fun startCtlWatcher() {
        CloudWatcher.addCallback("service_cloud_watcher", CloudCtlChangeCallback())
    }

    private fun stopCtlWatcher() {
        CloudWatcher.removeCallback("service_cloud_watcher")
    }

    private fun getCtlqFile() {
        try {
            if (Cloud.isConnected) {
                Cloud.getFile(Env.ctlqFilePath, Env.cloudCtlqPath)
            }
        } catch (e: Exception) {
            logE(className, "Can't download ctlq file: ${e.message}")
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotification(): Notification.Builder {
        return Notification.Builder(App.context, "main")
            .setContentTitle("AnTrack Service")
            .setContentText("Working")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setAutoCancel(true)
    }

    override fun onDestroy() {
        super.onDestroy()

        stopFileWatcher()
        stopCtlWatcher()

        if (!Settings.isServiceEnabled) {
            Alarm.cancel()
        }

        logD(className, "Service stopped")
        stopSelf()
    }

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }
}
