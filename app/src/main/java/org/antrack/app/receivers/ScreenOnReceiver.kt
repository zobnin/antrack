package org.antrack.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import org.antrack.app.functions.className
import org.antrack.app.functions.logD
import org.antrack.app.service.CloudService
import org.antrack.app.service.IntentActions

class ScreenOnReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        logD(className, "Screen On")
        CloudService.start(context, IntentActions.ACTION_SCREEN_ON)
    }
}
