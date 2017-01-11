package org.antrack.app.service;

import android.content.Intent;
import android.util.Base64;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

import org.antrack.app.C;
import org.antrack.app.Settings;
import org.antrack.app.libs.Crypto;
import org.antrack.app.libs.L;

public class OSService extends NotificationExtenderService {
    private static final String TAG = "OSService";

    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {
        L.d(TAG, "Received notification: " + receivedResult.payload.body);

        String cmd;

        if (!receivedResult.payload.body.equals("")) {
            try {
                cmd = Crypto.decryptString(
                        Base64.decode(receivedResult.payload.body, Base64.DEFAULT),
                        Settings.readKey());
            } catch (Exception e) {
                L.e(TAG, "Can't decrypt message: " + e.toString());
                return true;
            }
        } else {
            return true;
        }

        L.d(TAG, "Command: " + cmd);

        Intent intent = new Intent(this, MainService.class);
        intent.setAction(C.ACTION_PUSH);
        intent.putExtra("command", cmd);
        startService(intent);

        // Return true to stop the notification from displaying.
        return true;
    }
}
