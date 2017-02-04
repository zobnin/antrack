package org.antrack.app;

import android.app.Application;
import android.content.Context;

import com.onesignal.OneSignal;

public class AntrackApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        AntrackApplication.context = getApplicationContext();

        OneSignal.startInit(this).init();

        // Sync hashed email if you have a login system or collect it.
        //   Will be used to reach the user at the most optimal time of day.
        // OSignal.syncHashedEmail(userEmail);
    }

    public static Context getAppContext() {
        return AntrackApplication.context;
    }
}
