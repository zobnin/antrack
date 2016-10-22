package org.antrack.app.service.filters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.antrack.app.C;
import org.antrack.app.service.MainService;

public class ScreenOnReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ScreenOnReceiver", "Screen On");

        Intent myIntent = new Intent(context, MainService.class);
        myIntent.setAction(C.ACTION_SCREENON);
        context.startService(myIntent);
    }
}
