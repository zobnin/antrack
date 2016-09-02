package org.antrack.app.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class Alarm extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent sIntent = new Intent(context, MainService.class);
        sIntent.putExtra("alarm", true);

        context.startService(sIntent);
    }

    public static void set(Context context, long time) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), time, pIntent);
    }

    public static void cancel(Context context) {
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pIntent);
    }
}
