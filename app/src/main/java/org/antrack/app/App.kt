package org.antrack.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import org.antrack.app.service.Notifications

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
        Notifications.createChannel()
    }
}
