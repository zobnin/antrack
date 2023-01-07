package org.antrack.app.ui

import android.app.Activity
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import org.antrack.app.functions.toast

open class PermissionsActivity : Activity() {
    companion object {
        private const val REQUEST_PERMISSION = 1111
        private const val NEEDED_PERMISSIONS = 2222
        private const val REQUEST_CODE_ENABLE_ADMIN = 3333
    }

    private val adminComponent: ComponentName
        get() = ComponentName(this, PermissionReceiver::class.java)

    private var pCallback: (Boolean) -> Unit = {}
    private var permissionsNeed = listOf<String>()

    fun checkPermission(permission: String): Boolean {
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissions(
        permArray: Array<String>,
        callback: (Boolean) -> Unit,
    ) {
        pCallback = callback
        permissionsNeed = permArray
            .filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }

        if (permissionsNeed.isNotEmpty()) {
            requestNeededPermission(permissionsNeed)
        } else {
            pCallback(true)
        }
    }

    private fun requestNeededPermission(permissionsNeed: List<String>) {
        requestPermissions(
            permissionsNeed.toTypedArray(),
            NEEDED_PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        when (requestCode) {
            REQUEST_PERMISSION -> execCallback(grantResults)
            NEEDED_PERMISSIONS -> execCallback(grantResults)
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun execCallback(grantResults: IntArray) {
        pCallback(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
    }

    fun checkAdminPermission(): Boolean {
        val policyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return policyManager.isAdminActive(adminComponent)
    }

    fun requestAdminPermission() {
        try {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            }
            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
        } catch (e: Exception) {
            toast(e.message.toString())
        }
    }

    class PermissionReceiver : DeviceAdminReceiver() {
        override fun onDisabled(aContext: Context, aIntent: Intent) {
        }
    }
}