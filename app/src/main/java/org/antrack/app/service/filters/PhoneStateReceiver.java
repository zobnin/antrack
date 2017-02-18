package org.antrack.app.service.filters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.antrack.app.C;
import org.antrack.app.service.MainService;

public class PhoneStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String phoneNumber;

        Intent myIntent = new Intent(context, MainService.class);

        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){
            phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            if (phoneNumber != null)
                myIntent.setAction(C.ACTION_OUTGOINGCALL);
        } else {
            phoneNumber = intent.getStringExtra("incoming_number");
            if (phoneNumber != null)
                myIntent.setAction(C.ACTION_INCOMINGCALL);
        }

        myIntent.putExtra("phoneNumber", phoneNumber);
        context.startService(myIntent);
    }
}
