package org.antrack.app.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import app.R
import org.antrack.app.App
import org.antrack.app.C
import org.jetbrains.anko.notificationManager

internal object Notify {
    fun show(context: Context, title: String, txt: String, deviceName: String) {
        val yesIntent = Intent(context, MainService::class.java)
        yesIntent.action = C.ACTION_AUTH_DEVICE
        yesIntent.putExtra("device", deviceName)

        val noIntent = Intent(context, MainService::class.java)
        noIntent.action = C.ACTION_BAN_DEVICE
        noIntent.putExtra("device", deviceName)

        val pendingYesIntent = PendingIntent.getService(context, 0, yesIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val pendingNoIntent = PendingIntent.getService(context, 1, noIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        val yes = App.context!!.getString(R.string.yes)
        val no = App.context!!.getString(R.string.no)

        val mBuilder = Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(txt)
                .setSmallIcon(R.drawable.ic_notify_main)
                .setContentIntent(pendingYesIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_LIGHTS)
                .addAction(R.drawable.ic_yes, yes, pendingYesIntent)
                .addAction(R.drawable.ic_no, no, pendingNoIntent)

        val context = App.context
        context!!.notificationManager.notify(0, mBuilder.build())
    }

    fun cancel() {
        val context = App.context
        context!!.notificationManager.cancel(0)
    }
}
