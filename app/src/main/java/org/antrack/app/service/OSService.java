package org.antrack.app.service;

import android.content.Intent;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

import org.antrack.app.C;
import org.antrack.app.libs.L;

public class OSService extends NotificationExtenderService {
    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {
        L.d("OSService", "Received notification: " + receivedResult.payload.body);

        Intent intent = new Intent(this, MainService.class);
        intent.setAction(C.ACTION_PUSH);
        intent.putExtra("command", receivedResult.payload.body);
        this.startService(intent);

        // Return true to stop the notification from displaying.
        return true;
    }
}
