package org.antrack.app.service;

import android.content.Intent;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

import org.antrack.app.C;
import org.antrack.app.libs.L;

public class OSService extends NotificationExtenderService {
    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {
        Intent myIntent = new Intent(this, MainService.class);
        myIntent.setAction(C.ACTION_WAKEUP);
        this.startService(myIntent);

        L.d("OSService", "Received notification");

        // Return true to stop the notification from displaying.
        return true;
    }
}
