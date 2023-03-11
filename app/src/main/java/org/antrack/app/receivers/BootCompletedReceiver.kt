package org.antrack.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import org.antrack.app.Settings
import org.antrack.app.functions.className
import org.antrack.app.functions.logD
import org.antrack.app.service.CloudService

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            logD(className, "Booted")
            startServiceIfNeeded(context)
        }
    }

    private fun startServiceIfNeeded(context: Context) {
        if (Settings.startAtBoot) {
            CloudService.start(context)
        }
    }
}
