package org.antrack.app.libs

import android.app.Activity
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import org.antrack.app.App
import org.antrack.app.DEVICE_ADMIN_CODE

import org.antrack.app.functions.toast

class Admin {
    private var dpm: DevicePolicyManager = App.context
        .getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    private val deviceAdminComponent = ComponentName(
        App.context, AnTrackDeviceAdminReceiver::class.java
    )

    fun showDialog(activity: Activity) {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminComponent)
        }

        activity.startActivityForResult(intent, DEVICE_ADMIN_CODE)
    }

    val isActive: Boolean
        get() = dpm.isAdminActive(deviceAdminComponent)

    fun lock() {
        if (Build.VERSION.SDK_INT >= 26) {
            dpm.lockNow(DevicePolicyManager.FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY)
        } else {
            dpm.lockNow()
        }
    }

    fun wipe() {
        if (Build.VERSION.SDK_INT >= 29) {
            dpm.wipeData(
                DevicePolicyManager.WIPE_SILENTLY or
                    DevicePolicyManager.WIPE_EXTERNAL_STORAGE
            )
        } else {
            dpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE)
        }
    }

    class AnTrackDeviceAdminReceiver : DeviceAdminReceiver() {
        override fun onEnabled(context: Context, intent: Intent) {
            context.toast("Admin rights granted")
        }

        override fun onDisabled(context: Context, intent: Intent) {
            context.toast("Admin rights disabled")
        }
    }
}
