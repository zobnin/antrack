package org.antrack.app.service;

import android.content.Intent;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

import org.antrack.app.C;
import org.antrack.app.libs.L;

public class OSService extends NotificationExtenderService {
    private static final String TAG = "OSService";

    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {
        L.d(TAG, "Received notification: " + receivedResult.payload.body);

        // Message format: "device_name encrypted_command"
        String[] message = receivedResult.payload.body.split(" ");

        if (message.length < 2) {
            L.d(TAG, "Invalid message");
            return true;
        }

        Intent intent = new Intent(this, MainService.class);
        intent.setAction(C.ACTION_PUSH);
        intent.putExtra("device", message[0]);
        intent.putExtra("message", message[1]);
        startService(intent);

        // Return true to stop the notification from displaying.
        return true;
    }
}
