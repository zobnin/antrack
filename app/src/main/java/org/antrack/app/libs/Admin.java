package org.antrack.app.libs;

import android.app.Activity;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import org.antrack.app.C;
import org.antrack.app.Settings;

public class Admin {
    Context context;
    DevicePolicyManager mDPM;
    ComponentName mDeviceAdmin;

    public Admin(Context context) {
        this.context = context;

        mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    public boolean showDialog(Activity activity) {
        mDeviceAdmin = new ComponentName(activity, myDeviceAdminReceiver.class);

        // Launch the activity to have the user enable our admin.
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
        //intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
        //        mActivity.getString(R.string.add_admin_extra_app_text));

        activity.startActivityForResult(intent, 1);

        return true;
    }

    public boolean isActive() {
        return mDPM.isAdminActive(mDeviceAdmin);
    }

    public void wipe() {
        mDPM.wipeData(0);
    }

    public void setMaxPassAttempts(int max) {
        mDPM.setMaximumFailedPasswordsForWipe(mDeviceAdmin, max);
    }

    public boolean setPin(String pin) {
        if (pin.matches("[0-9]{4}"))
            return mDPM.resetPassword(pin, DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
        return false;
    }

    public boolean lock() {
        mDPM.lockNow();
        return true;
    }

    public static class myDeviceAdminReceiver extends DeviceAdminReceiver {
        @Override
        public void onEnabled(Context context, Intent intent) {
            Utils.showToast(context, "Admin rights granted");
            Settings.getInstance().put(C.S_USE_ADMIN, "true");
        }
/*
        @Override
        public CharSequence onDisableRequested(Context context, Intent intent) {
            return context.getString(R.string.admin_receiver_status_disable_warning);
        }
*/
        @Override
        public void onDisabled(Context context, Intent intent) {
            Utils.showToast(context, "Admin rights disabled");
            Settings.getInstance().put(C.S_USE_ADMIN, "false");
        }
/*
        @Override
        public void onPasswordChanged(Context context, Intent intent) {
            showToast(context, context.getString(R.string.admin_receiver_status_pw_changed));
        }

        @Override
        public void onPasswordFailed(Context context, Intent intent) {
            showToast(context, context.getString(R.string.admin_receiver_status_pw_failed));
        }

        @Override
        public void onPasswordSucceeded(Context context, Intent intent) {
            showToast(context, context.getString(R.string.admin_receiver_status_pw_succeeded));
        }
*/
    }
}
