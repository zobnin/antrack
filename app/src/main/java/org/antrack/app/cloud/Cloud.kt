package org.antrack.app.cloud

import android.app.Activity
import org.antrack.app.App
import org.antrack.app.cloud.provider.Dropbox
import org.antrack.app.functions.className
import org.antrack.app.functions.isNetConnected
import org.antrack.app.functions.logD
import org.antrack.app.functions.sleepS
import java.io.InputStream

object Cloud {
    const val DROPBOX = "dropbox"
    private const val MAX_ONLINE_WAIT = 320

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

        if (providerName == DROPBOX) {
            connectDropbox(token)
        }
    }

    fun auth(activity: Activity, providerName: String) {
        if (providerName == DROPBOX) {
            authDropbox(activity)
        }
    }

    fun resume(): String? {
        return provider?.resumeAuth()
    }

    fun putFile(lFile: String, rFile: String) {
        logD(className, "Put file $lFile as $rFile")
        provider?.putFile(lFile, rFile)
    }

    fun putFile(iStream: InputStream, rFile: String) {
        logD(className, "Put stream as $rFile")
        provider?.putFile(iStream, rFile)
    }

    fun getFile(rFile: String, lFile: String) {
        logD(className, "Get file $rFile as $lFile")
        provider?.getFile(rFile, lFile)
    }

    fun getMetadata(rFile: String): CloudMetadata? {
        try {
            logD(className, "Get metadata: $rFile")
            return provider?.getMetadata(rFile)
        } catch (e: Exception) {
            logD(className, "Get metadata error: ${e.message}")
            return null
        }
    }

    fun watchForChanges(dir: String): List<String>? {
        logD(className, "Start watching")
        return provider?.watchForChanges(dir)
    }

    @Synchronized
    fun waitOnline() {
        var seconds = 10
        while (!App.context.isNetConnected()) {
            logD(className, "No internet, sleep $seconds seconds")
            sleepS(seconds)

            if (seconds < MAX_ONLINE_WAIT)
                seconds *= 2
        }
    }

    private fun connectDropbox(token: String) {
        provider = Dropbox(token)
        isConnected = getConnectionStatus()

        if (isConnected) {
            logD(className, "Connected to cloud")
        }
    }

    private fun getConnectionStatus(): Boolean {
        return provider?.getStatus()?.isConnected ?: false
    }

    private fun authDropbox(activity: Activity) {
        provider = Dropbox().apply {
            auth(activity)
        }
    }
}
