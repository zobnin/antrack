package org.antrack.app.service.filters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import org.antrack.app.C
import org.antrack.app.Settings
import org.antrack.app.libs.L
import org.antrack.app.service.Logger

/* 1. детектируем
     Intent: android.intent.action.SIM_STATE_CHANGED with extras: ss = NOT_READY
     Intent: android.intent.action.SIM_STATE_CHANGED with extras: ss = READY
   2. записываем imsi когда симка готова
   3. если imsi изменился алертуем
*/

// FIXME Здесь надо автоматически включать режим lost

class SimChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        L.d("SimChangeReceiver", "SIM state changed")

        val extras = intent.extras
        val ready = extras.getString("ss")

        if (ready != null && ready == "READY") {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val IMSI = tm.subscriberId

            if (Settings[C.S_IMSI].isNullOrEmpty()) {
                Settings.put(C.S_IMSI, IMSI)
            } else {
                val oldIMSI = Settings[C.S_IMSI]

                if (oldIMSI != IMSI) {
                    Logger.simChanged()
                    val needSms = Settings[C.S_SMS_ON_SIM_CHANGE]

                    if (!needSms.isNullOrEmpty() && needSms == C.TRUE) {
                        val number = Settings[C.S_BACKUP_PHONE]
                        if (!number.isNullOrEmpty()) {
                            val model = android.os.Build.MODEL
                            SmsManager.getDefault().sendTextMessage(number, null, "Sim change on $model detected!", null, null)
                        }
                    }
                }
            }
        }
    }
}
