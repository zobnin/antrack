package org.antrack.app.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.antrack.app.AntrackApplication;
import org.antrack.app.C;

import app.R;

class Notify {
    static public void show(Context context, String title, String txt, String deviceName) {
        Intent yesIntent = new Intent(context, MainService.class);
        yesIntent.setAction(C.ACTION_AUTH_DEVICE);
        yesIntent.putExtra("device", deviceName);

        Intent noIntent = new Intent(context, MainService.class);
        noIntent.setAction(C.ACTION_BAN_DEVICE);
        noIntent.putExtra("device", deviceName);

        PendingIntent pendingYesIntent = PendingIntent.getService(context, 0, yesIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pendingNoIntent = PendingIntent.getService(context, 1, noIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        String yes = AntrackApplication.getAppContext().getString(R.string.yes);
        String no  = AntrackApplication.getAppContext().getString(R.string.no);

        Notification.Builder mBuilder = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(txt)
                .setSmallIcon(R.drawable.ic_notify_main)
                .setContentIntent(pendingYesIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
                .addAction(R.drawable.ic_yes, yes, pendingYesIntent)
                .addAction(R.drawable.ic_no, no, pendingNoIntent);

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(0, mBuilder.build());
    }
}
