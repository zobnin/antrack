package org.antrack.app.libs;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.antrack.app.service.MainService;

public class Notify {
    private static Notification.Builder mBuilder;

    static public void show(Context context, String title, String txt, String action) {
        mBuilder = new Notification.Builder(context)
                        .setContentTitle(title)
                        .setContentText(txt);

        //mBuilder.setSmallIcon(R.drawable.notification_icon);

        Intent notificationIntent = new Intent(context, MainService.class);

        if (action != null)
            notificationIntent.setAction(action);

        // since the only thing changing in the Intent is the extras,
        // the PendingIntent.getService(...) factory method is simply re-using the old intent as an optimization.
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }
}
