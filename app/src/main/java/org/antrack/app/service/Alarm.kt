package org.antrack.app.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import org.antrack.app.App

import org.antrack.app.C

class Alarm : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sIntent = Intent(context, MainService::class.java)
        sIntent.action = C.ACTION_ALARM

        context.startService(sIntent)
    }

    companion object {
        private var pIntent: PendingIntent? = null

        fun set(time: Long) {
            val context = App.context
            val am = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, Alarm::class.java)
            pIntent = PendingIntent.getBroadcast(context, 0, intent, 0)

            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), time, pIntent)
        }

        fun cancel(context: Context) {
            if (pIntent != null) {
                val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                am.cancel(pIntent)
            }
        }
    }
}
