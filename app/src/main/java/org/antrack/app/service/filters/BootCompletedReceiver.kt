package org.antrack.app.service.filters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import org.antrack.app.C
import org.antrack.app.Settings
import org.antrack.app.service.Logger
import org.antrack.app.service.MainService

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        Logger.booted()

        val startAtBoot = Settings.get(C.S_START_AT_BOOT)
        if (startAtBoot == C.FALSE) {
            return
        }

        val enabled = Settings.get(C.S_ENABLE_SERVICE)
        if (enabled == C.FALSE) {
            return
        }

        val myIntent = Intent(context, MainService::class.java)
        myIntent.action = C.ACTION_BOOT
        context.startService(myIntent)
    }
}
