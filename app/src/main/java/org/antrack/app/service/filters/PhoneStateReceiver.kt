package org.antrack.app.service.filters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import org.antrack.app.C
import org.antrack.app.service.MainService

class PhoneStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val phoneNumber: String?

        val myIntent = Intent(context, MainService::class.java)

        if (intent.action == Intent.ACTION_NEW_OUTGOING_CALL) {
            phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
            if (phoneNumber != null)
                myIntent.action = C.ACTION_OUTGOINGCALL
        } else {
            phoneNumber = intent.getStringExtra("incoming_number")
            if (phoneNumber != null)
                myIntent.action = C.ACTION_INCOMINGCALL
        }

        myIntent.putExtra("phoneNumber", phoneNumber)
        context.startService(myIntent)
    }
}
