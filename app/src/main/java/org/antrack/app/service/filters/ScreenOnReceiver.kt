package org.antrack.app.service.filters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import org.antrack.app.C
import org.antrack.app.libs.L
import org.antrack.app.service.MainService

class ScreenOnReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        L.d("ScreenOnReceiver", "Screen On")

        val myIntent = Intent(context, MainService::class.java)
        myIntent.action = C.ACTION_SCREENON
        context.startService(myIntent)
    }
}
