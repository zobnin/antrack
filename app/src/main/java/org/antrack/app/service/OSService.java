package org.antrack.app.service;

import android.content.Intent;
import android.util.Base64;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

import org.antrack.app.C;
import org.antrack.app.Init;
import org.antrack.app.Pw;
import org.antrack.app.libs.Crypto;
import org.antrack.app.libs.Files;
import org.antrack.app.libs.L;

import java.io.File;
import java.security.PublicKey;

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

        String remoteDeviceName = message[0];
        String remoteEncMessage = message[1];

        // DEBUG
        // FIXME icon
        //Notify.show(getApplicationContext(), "auth", "auth", "auth");

        String cmd;

        if (!new File(Init.DEVICES_DIR + remoteDeviceName + C.PUBLIC_KEY_FILE).exists()) {
            Files.mkdir(Init.DEVICES_DIR + remoteDeviceName);
            Pw pw = Pw.getInstance();

            if (pw.isConnected()) {
                try {
                    pw.getFile(Init.DEVICES_DIR + remoteDeviceName + C.PUBLIC_KEY_FILE,
                            "/" + remoteDeviceName + C.PUBLIC_KEY_FILE);
                } catch (InterruptedException e) {
                    L.e(TAG, "Can't download public key: " + e.toString());
                    return true;
                }
            } else {
                L.e(TAG, "Can't download public key: not connected");
                return true;
            }
        }

        try {
            cmd = Crypto.decryptStringRSA(
                    Base64.decode(remoteEncMessage, Base64.DEFAULT),
                    readPublicKey(remoteDeviceName));
        } catch (Exception e) {
            L.e(TAG, "Can't decrypt message: " + e.toString());
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

    private PublicKey readPublicKey(String deviceName) {
        PublicKey publicKey = null;

        try {
            String stringKey = Files.readTextFile(Init.DEVICES_DIR + deviceName + C.PUBLIC_KEY_FILE);
            publicKey = Crypto.stringToPublicKey(stringKey.trim());
        } catch (Exception e) {
            L.e(TAG, "Can't read public key file: " + e.toString());
        }
        return publicKey;
    }
}
