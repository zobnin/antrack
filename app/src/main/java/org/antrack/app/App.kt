package org.antrack.app

import android.app.Application
import android.content.Context

import com.onesignal.OneSignal

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        App.context = applicationContext
        OneSignal.startInit(this).init()

        // Sync hashed email if you have a login system or collect it.
        //   Will be used to reach the user at the most optimal time of day.
        // OSignal.syncHashedEmail(userEmail);
    }

    companion object {
        var context: Context? = null
            private set
    }
}
