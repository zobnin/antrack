package org.antrack.app.libs;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;

public class Checks {
    private static final String TAG = "Checks";
    private static final String SIGNATURE = "PN1RUozuVqArQa6drULZRbOErqI";
    private static final String PLAY_STORE_APP_ID = "com.android.vending";

    public static boolean all(Context context) {
        boolean signature = checkSignature(context);
        boolean fromPlayStore = checkInstaller(context);
        boolean isEmulator = checkEmulator();
        boolean isDebuggable = checkDebuggable(context);

        // DEBUG
        L.e(TAG, "signature: " + signature);
        L.e(TAG, "fromPlayStore: " + fromPlayStore);
        L.e(TAG, "isEmulator: " + isEmulator);
        L.e(TAG, "isDebuggable: " + isDebuggable);

        return signature &&
                fromPlayStore &&
                !isEmulator &&
                !isDebuggable;
    }

    public static boolean checkSignature(Context context) {
        return SIGNATURE.equals(getSignature(context));
    }

    public static String getSignature(Context context) {
        String apkSignature = null;
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(),
                            PackageManager.GET_SIGNATURES);

            for (Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                apkSignature = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                L.e("DEBUG", "SIGNATURE: " + apkSignature);
            }
        } catch (Exception e) {}
        return apkSignature;
    }

    public static boolean checkInstaller(Context context) {
            final String installer = context.getPackageManager()
                    .getInstallerPackageName(context.getPackageName());

            return installer != null
                    && installer.startsWith(PLAY_STORE_APP_ID);

    }

    public static boolean checkEmulator() {
        try {
            boolean goldfish = getSystemProperty("ro.hardware").contains("goldfish");
            boolean emu = getSystemProperty("ro.kernel.qemu").length() > 0;
            boolean sdk = getSystemProperty("ro.product.model").equals("sdk");

            if (emu || goldfish || sdk) {
                return true;
            }
        } catch (Exception e) {}
        return false;
    }

    public static boolean checkDebuggable(Context context){
        return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;

    }

    private static String getSystemProperty(String name) throws Exception {
        Class systemPropertyClazz = Class.forName("android.os.SystemProperties");
        return (String) systemPropertyClazz.getMethod("get", new Class[]{String.class})
                .invoke(systemPropertyClazz, new Object[]{name});
    }
}
