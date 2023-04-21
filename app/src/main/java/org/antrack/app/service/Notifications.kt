package org.antrack.app.service

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import app.R
import org.antrack.app.App

object Notifications {
    fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val name = App.context.getString(R.string.app_name)
            val descriptionText = App.context.getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("main", name, importance).apply {
                description = descriptionText
            }
            (App.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    fun createServiceNotification(): Notification.Builder {
        return Notification.Builder(App.context, "main")
            .setContentTitle("AnTrack Service")
            .setContentText("Working")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setAutoCancel(true)
    }
}