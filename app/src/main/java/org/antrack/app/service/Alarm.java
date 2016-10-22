package org.antrack.app.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import org.antrack.app.C;

public class Alarm extends BroadcastReceiver {
    private static PendingIntent pIntent = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent sIntent = new Intent(context, MainService.class);
        sIntent.setAction(C.ACTION_ALARM);

        context.startService(sIntent);
    }

    public static void set(Context context, long time) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, Alarm.class);
        pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), time, pIntent);
    }

    public static void cancel(Context context) {
        if (pIntent != null) {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.cancel(pIntent);
        }
    }
}
