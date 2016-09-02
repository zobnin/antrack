package org.antrack.app.service.filters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.antrack.app.C;
import org.antrack.app.service.Logger;
import org.antrack.app.service.MainService;
import org.antrack.app.libs.Net;
import org.antrack.app.Settings;

public class ConnChangeReceiver extends BroadcastReceiver {
    private String TAG="ConnChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Settings.init();

        String enabled = Settings.get(C.S_ENABLE_SERVICE);
        if (enabled != null && enabled.equals("false")) {
            return;
        }

        Intent myIntent = new Intent(context, MainService.class);

        if (Net.isConnected(context)) {
            Log.d(TAG, "Connected to network, start service");
            Logger.connected(context);
            context.startService(myIntent);
        } else {
            Log.d(TAG, "Network disconnected, stop service");
            Logger.disconnected(context);
            context.stopService(myIntent);
        }
    }
}
