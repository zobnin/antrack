package org.antrack.app

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import app.R

class App : Application() {
    companion object {
        // App context exists as long as app works so its not a memory leak
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
            private set

        val dataDir: String
            get() = context.applicationInfo.dataDir
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val name = getString(R.string.app_name)
            val descriptionText = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("main", name, importance).apply {
                description = descriptionText
            }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }
}
