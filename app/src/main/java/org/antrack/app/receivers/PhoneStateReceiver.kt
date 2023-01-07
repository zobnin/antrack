@file:Suppress("DEPRECATION")

package org.antrack.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import org.antrack.app.service.CloudService
import org.antrack.app.service.IntentActions

class PhoneStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = when (intent.action) {
            Intent.ACTION_NEW_OUTGOING_CALL -> context.getIntentForOutgoingCall(intent)
            else -> context.getIntentForIncomingCall(intent)
        }

        if (serviceIntent != null) {
            context.startService(serviceIntent)
        }
    }

    private fun Context.getIntentForIncomingCall(intent: Intent): Intent? {
        val phoneNumber = intent.getStringExtra("incoming_number") ?: return null

        return Intent(this, CloudService::class.java).apply {
            action = IntentActions.ACTION_INCOMING_CALL
            putExtra(IntentActions.EXTRA_PHONE_NUMBER, phoneNumber)
        }
    }

    private fun Context.getIntentForOutgoingCall(intent: Intent): Intent? {
        val phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER) ?: return null

        return Intent(this, CloudService::class.java).apply {
            action = IntentActions.ACTION_OUTGOING_CALL
            putExtra(IntentActions.EXTRA_PHONE_NUMBER, phoneNumber)
        }
    }
}
