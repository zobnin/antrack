package org.antrack.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.antrack.app.functions.className
import org.antrack.app.functions.logD

/* 1. детектируем
     Intent: android.intent.action.SIM_STATE_CHANGED with extras: ss = NOT_READY
     Intent: android.intent.action.SIM_STATE_CHANGED with extras: ss = READY
   2. записываем imsi когда симка готова
   3. если imsi изменился алертуем
*/

// FIXME Здесь надо автоматически включать режим lost

class SimChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        logD(className, "SIM state changed")

        val extras = intent.extras
        val ready = extras?.getString("ss")

        if (ready != null && ready == "READY") {
            /*
            if (Settings[C.S_SMS_ON_SIM_CHANGE] != FALSE) {
                val number = Settings[C.S_BACKUP_PHONE]
                if (!number.isNullOrEmpty()) {
                    val model = android.os.Build.MODEL
                    SmsManager.getDefault()
                        .sendTextMessage(number, null, "Sim change on $model detected!", null, null)
                }
            }*/
        }
    }
}
