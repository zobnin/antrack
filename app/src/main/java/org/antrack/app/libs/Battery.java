package org.antrack.app.libs;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;

public class Battery {
    @TargetApi(23)
    public static void requestIgnoreBatteryOptimisation(Context context) {
        Intent intent = new Intent();
        String packageName = context.getPackageName();
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
            context.startActivity(intent);
        }
    }

    @TargetApi(23)
    public static void openBatteryOptimizationSettings(Context context) {
        Intent intent = new Intent();
        String packageName = context.getPackageName();
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            context.startActivity(intent);
        }
    }
}
