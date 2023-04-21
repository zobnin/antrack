package org.antrack.app.functions

import android.content.Context
import android.net.ConnectivityManager

fun Context.isNetConnected(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = cm.activeNetworkInfo
    return networkInfo?.isConnected ?: false
}
