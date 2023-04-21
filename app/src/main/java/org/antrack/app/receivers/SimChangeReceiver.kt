package org.antrack.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.antrack.app.functions.className
import org.antrack.app.functions.logD

class SimChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        logD(className, "SIM state changed")

        val extras = intent.extras
        val ready = extras?.getString("ss")

        if (ready != null && ready == "READY") {
            logD(className, "Sim changed!")
        }
    }
}
