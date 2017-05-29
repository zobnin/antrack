package org.antrack.app.service

import android.content.Intent

import com.onesignal.NotificationExtenderService
import com.onesignal.OSNotificationReceivedResult

import org.antrack.app.C
import org.antrack.app.libs.L

class OSService : NotificationExtenderService() {
    override fun onNotificationProcessing(receivedResult: OSNotificationReceivedResult): Boolean {
        L.d(TAG, "Received notification: " + receivedResult.payload.body)

        // Message format: "device_name encrypted_command"
        val message = receivedResult.payload.body.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (message.size < 2) {
            L.d(TAG, "Invalid message")
            return true
        }

        val intent = Intent(this, MainService::class.java)
        intent.action = C.ACTION_PUSH
        intent.putExtra("device", message[0])
        intent.putExtra("message", message[1])
        startService(intent)

        // Return true to stop the notification from displaying.
        return true
    }

    companion object {
        private val TAG = "OSService"
    }
}
