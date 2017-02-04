package org.antrack.app.service.filters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

import org.antrack.app.C;
import org.antrack.app.Settings;
import org.antrack.app.libs.L;
import org.antrack.app.service.Logger;

/* 1. детектируем
     Intent: android.intent.action.SIM_STATE_CHANGED with extras: ss = NOT_READY
     Intent: android.intent.action.SIM_STATE_CHANGED with extras: ss = READY
   2. записываем imsi когда симка готова
   3. если imsi изменился алертуем
*/

// FIXME Здесь надо автоматически включать режим lost

public class SimChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        L.d("SimChangeReceiver", "SIM state changed");

        Bundle extras = intent.getExtras();
        String ready = extras.getString("ss");

        if (ready != null && ready.equals("READY")) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String IMSI = tm.getSubscriberId();

            Settings settings = Settings.getInstance();

            if (settings.get(C.S_IMSI) == null) {
                settings.put(C.S_IMSI, IMSI);
            } else {
                String oldIMSI = settings.get(C.S_IMSI);

                if (!oldIMSI.equals(IMSI)) {
                    Logger.simChanged(context);
                    String needSms = settings.get(C.S_SMS_ON_SIM_CHANGE);

                    if (needSms != null && needSms.equals(C.TRUE)) {
                        String number = settings.get(C.S_BACKUP_PHONE);
                        if (number != null) {
                            String model = android.os.Build.MODEL;
                            SmsManager.getDefault().sendTextMessage(number, null, "Sim change on " + model + " detected!", null, null);
                        }
                    }
                }
            }
        }
    }
}
