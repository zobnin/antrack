package org.antrack.app.service.filters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import org.antrack.app.C
import org.antrack.app.Settings
import org.antrack.app.libs.L
import org.antrack.app.libs.Net
import org.antrack.app.service.Logger
import org.antrack.app.service.MainService

class ConnChangeReceiver : BroadcastReceiver() {
    private val TAG = "ConnChangeReceiver"

    override fun onReceive(context: Context, intent: Intent) {

        if (Settings[C.S_ENABLE_SERVICE] == C.FALSE) {
            return
        }

        val myIntent = Intent(context, MainService::class.java)

        if (Net.isConnected) {

            L.d(TAG, "Connected to network, start service")
            Logger.connected()
            context.startService(myIntent)

        } else {

            L.d(TAG, "Network disconnected, stop service")
            Logger.disconnected()
            context.stopService(myIntent)
        }
    }
}
