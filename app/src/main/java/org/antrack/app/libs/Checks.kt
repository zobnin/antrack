package org.antrack.app.libs

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Base64
import java.security.MessageDigest

object Checks {
    private val TAG = "Checks"
    private val SIGNATURE = "PN1RUozuVqArQa6drULZRbOErqI="
    private val PLAY_STORE_APP_ID = "com.android.vending"

    fun all(context: Context): Boolean {
        val signature = checkSignature(context)
        val fromPlayStore = checkInstaller(context)
        val isEmulator = checkEmulator()
        val isDebuggable = checkDebuggable(context)

        // DEBUG
        L.e(TAG, "signature: " + signature)
        L.e(TAG, "fromPlayStore: " + fromPlayStore)
        L.e(TAG, "isEmulator: " + isEmulator)
        L.e(TAG, "isDebuggable: " + isDebuggable)

        return signature &&
                fromPlayStore &&
                !isEmulator &&
                !isDebuggable
    }

    fun checkSignature(context: Context): Boolean {
        return SIGNATURE == getSignature(context)!!.trim { it <= ' ' }
    }

    fun getSignature(context: Context): String? {
        var apkSignature: String? = null
        try {
            val packageInfo = context.packageManager
                    .getPackageInfo(context.packageName,
                            PackageManager.GET_SIGNATURES)

            for (signature in packageInfo.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                apkSignature = Base64.encodeToString(md.digest(), Base64.DEFAULT)
                L.e("DEBUG", "SIGNATURE: " + apkSignature!!)
            }
        } catch (e: Exception) {
        }

        return apkSignature
    }

    fun checkInstaller(context: Context): Boolean {
        val installer = context.packageManager
                .getInstallerPackageName(context.packageName)

        return installer != null && installer.startsWith(PLAY_STORE_APP_ID)

    }

    fun checkEmulator(): Boolean {
        try {
            val goldfish = getSystemProperty("ro.hardware").contains("goldfish")
            val emu = getSystemProperty("ro.kernel.qemu").length > 0
            val sdk = getSystemProperty("ro.product.model") == "sdk"

            if (emu || goldfish || sdk) {
                return true
            }
        } catch (e: Exception) {
        }

        return false
    }

    fun checkDebuggable(context: Context): Boolean {
        return context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

    }

    @Throws(Exception::class)
    private fun getSystemProperty(name: String): String {
        val systemPropertyClazz = Class.forName("android.os.SystemProperties")
        return systemPropertyClazz.getMethod("get", *arrayOf<Class<*>>(String::class.java))
                .invoke(systemPropertyClazz, *arrayOf<Any>(name)) as String
    }
}
