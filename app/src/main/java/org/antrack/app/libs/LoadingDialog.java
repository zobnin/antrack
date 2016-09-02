package org.antrack.app.libs;

import android.app.Activity;
import android.app.ProgressDialog;

public class LoadingDialog {
    private static ProgressDialog loadingDialog;

    public static void show(final Activity act, final String message) {
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingDialog = new ProgressDialog(act);
                loadingDialog.setMessage(message);
                loadingDialog.setCancelable(false);
                loadingDialog.setInverseBackgroundForced(false);
                loadingDialog.show();
            }
        });
    }

    public static void hide(final Activity act) {
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loadingDialog != null) {
                    loadingDialog.hide();
                    // GC
                    loadingDialog = null;
                }
            }
        });
    }

    public static boolean isShown() {
        return loadingDialog != null;
    }

}
