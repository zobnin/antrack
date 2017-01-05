package org.antrack.app.service;

import android.content.Intent;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

import org.antrack.app.C;

public class OSService extends NotificationExtenderService {
    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {
        Intent myIntent = new Intent(this, MainService.class);
        myIntent.setAction(C.ACTION_WAKEUP);
        this.startService(myIntent);

        // Return true to stop the notification from displaying.
        return true;
    }
}
