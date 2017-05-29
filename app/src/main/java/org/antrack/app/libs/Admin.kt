package org.antrack.app.libs

import android.app.Activity
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

import org.antrack.app.C
import org.antrack.app.Settings

class Admin(internal var context: Context) {
    internal var mDPM: DevicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    internal lateinit var mDeviceAdmin: ComponentName

    fun showDialog(activity: Activity): Boolean {
        mDeviceAdmin = ComponentName(activity, myDeviceAdminReceiver::class.java)

        // Launch the activity to have the user enable our admin.
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin)
        //intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
        //        mActivity.getString(R.string.add_admin_extra_app_text));

        activity.startActivityForResult(intent, 1)

        return true
    }

    val isActive: Boolean
        get() = mDPM.isAdminActive(mDeviceAdmin)

    fun wipe() {
        mDPM.wipeData(0)
    }

    fun setMaxPassAttempts(max: Int) {
        mDPM.setMaximumFailedPasswordsForWipe(mDeviceAdmin, max)
    }

    fun setPin(pin: String): Boolean {
        if (pin.matches("[0-9]{4}".toRegex()))
            return mDPM.resetPassword(pin, DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY)
        return false
    }

    fun lock(): Boolean {
        mDPM.lockNow()
        return true
    }

    class myDeviceAdminReceiver : DeviceAdminReceiver() {
        override fun onEnabled(context: Context, intent: Intent) {
            Utils.showToast(context, "Admin rights granted")
            Settings.put(C.S_USE_ADMIN, "true")
        }

        /*
        @Override
        public CharSequence onDisableRequested(Context context, Intent intent) {
            return context.getString(R.string.admin_receiver_status_disable_warning);
        }
*/
        override fun onDisabled(context: Context, intent: Intent) {
            Utils.showToast(context, "Admin rights disabled")
            Settings.put(C.S_USE_ADMIN, "false")
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
