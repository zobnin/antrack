package org.antrack.app.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import org.antrack.app.App

class Alarm : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        CloudService.start(context, IntentActions.ACTION_ALARM)
    }

    companion object {
        private val am by lazy { App.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager }

        private val pIntent by lazy {
            PendingIntent.getBroadcast(
                App.context,
                0,
                Intent(App.context, Alarm::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        }

        fun set(time: Long) {
            am.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                time,
                pIntent
            )
        }

        fun cancel() {
            if (pIntent != null) {
                am.cancel(pIntent)
            }
        }
    }
}
