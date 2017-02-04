package org.antrack.app.service.filters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.antrack.app.C;
import org.antrack.app.Settings;
import org.antrack.app.service.Logger;
import org.antrack.app.service.MainService;

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Settings settings = Settings.getInstance();

        Logger.booted(context);

        String startAtBoot = settings.get(C.S_START_AT_BOOT);
        if(startAtBoot != null && startAtBoot.equals(C.FALSE)) {
            return;
        }

        String enabled = settings.get(C.S_ENABLE_SERVICE);
        if(enabled != null && enabled.equals(C.FALSE)) {
            return;
        }

        Intent myIntent = new Intent(context, MainService.class);
        myIntent.setAction(C.ACTION_BOOT);
        context.startService(myIntent);
    }
}
