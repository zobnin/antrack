package org.antrack.app.cloud

import android.app.Activity
import org.antrack.app.App
import org.antrack.app.Settings
import org.antrack.app.cloud.provider.Dropbox
import org.antrack.app.functions.className
import org.antrack.app.functions.isNetConnected
import org.antrack.app.functions.logD
import org.antrack.app.functions.sleepS

object Cloud {
    private var provider: ICloudProvider? = null

    var isConnected = false
        private set

    fun connect(providerName: String, token: String) {
        if (isConnected) {
            return
        }

        if (token.isBlank()) {
            throw IllegalStateException("Token is empty")
        }

        if (providerName == "dropbox") {
            provider = Dropbox(token)
            isConnected = getConnectionStatus()

            if (isConnected) {
                logD(className, "Connected to cloud")
                return
            }
        }
    }

    private fun getConnectionStatus(): Boolean {
        return provider?.getStatus()?.isConnected ?: false
    }

    fun auth(activity: Activity) {
        if (Settings.plugin == "dropbox") {
            provider = Dropbox().apply {
                auth(activity)
            }
        }
    }

    fun resume(): String? {
        return provider?.resumeAuth()
    }

    fun putFile(lFile: String, rFile: String) {
        logD(className, "Put file $lFile as $rFile")
        provider?.putFile(lFile, rFile)
    }

    fun getFile(lFile: String, rFile: String) {
        logD(className, "Get file $rFile as $lFile")
        provider?.getFile(lFile, rFile)
    }

    fun watchForChanges(dir: String): List<String>? {
        logD(className, "Start watching")
        return provider?.watchForChanges(dir)
    }

    private const val MAX_SLEEP = 320

    @Synchronized
    fun waitOnline() {
        var seconds = 10
        while (!App.context.isNetConnected()) {
            logD(className, "No internet, sleep $seconds seconds")
            sleepS(seconds)

            if (seconds < MAX_SLEEP)
                seconds *= 2
        }
    }
}
