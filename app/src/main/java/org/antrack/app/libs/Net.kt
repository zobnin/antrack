package org.antrack.app.libs

import android.content.Context
import android.net.ConnectivityManager
import org.antrack.app.App
import java.io.IOException

object Net {
    val isOnline: Boolean
        get() {
            val runtime = Runtime.getRuntime()
            try {
                val ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
                val exitValue = ipProcess.waitFor()
                return exitValue == 0

            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            return false
        }

    val isConnected: Boolean
        get() {
            val cm = App.context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager ?: return false
            val networkInfo = cm.activeNetworkInfo
            return networkInfo.isConnected
        }
}
