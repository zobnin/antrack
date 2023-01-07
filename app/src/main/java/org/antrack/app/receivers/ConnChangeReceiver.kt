package org.antrack.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import org.antrack.app.functions.className
import org.antrack.app.functions.isNetConnected
import org.antrack.app.functions.logD
import org.antrack.app.service.CloudService

class ConnChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (context.isNetConnected()) {
            logD(className, "Connected to network, start service")
            CloudService.start(context)
        } else {
            logD(className, "Network disconnected, stop service")
            CloudService.stop(context)
        }
    }
}
