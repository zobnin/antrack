package org.antrack.app.service.filters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.antrack.app.C;
import org.antrack.app.service.Logger;
import org.antrack.app.service.MainService;
import org.antrack.app.Settings;

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Settings.init();
        Logger.booted(context);

        String startAtBoot = Settings.get(C.S_START_AT_BOOT);
        if(startAtBoot != null && startAtBoot.equals("false")) {
            return;
        }

        String enabled = Settings.get(C.S_ENABLE_SERVICE);
        if(enabled != null && enabled.equals("false")) {
            return;
        }

        Intent myIntent = new Intent(context, MainService.class);
        myIntent.putExtra("boot", true);
        context.startService(myIntent);
    }
}
