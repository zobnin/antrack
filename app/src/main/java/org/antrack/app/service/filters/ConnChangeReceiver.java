package org.antrack.app.service.filters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.antrack.app.C;
import org.antrack.app.Settings;
import org.antrack.app.libs.L;
import org.antrack.app.libs.Net;
import org.antrack.app.service.Logger;
import org.antrack.app.service.MainService;

public class ConnChangeReceiver extends BroadcastReceiver {
    private String TAG="ConnChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String enabled = Settings.getInstance().get(C.S_ENABLE_SERVICE);
        if (enabled != null && enabled.equals(C.FALSE)) {
            return;
        }

        Intent myIntent = new Intent(context, MainService.class);

        if (Net.isConnected(context)) {
            L.d(TAG, "Connected to network, start service");
            Logger.connected(context);
            context.startService(myIntent);
        } else {
            L.d(TAG, "Network disconnected, stop service");
            Logger.disconnected(context);
            context.stopService(myIntent);
        }
    }
}
