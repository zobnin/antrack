package org.antrack.app.functions

import android.annotation.SuppressLint
import android.content.Context

val Context.androidId: String?
    @SuppressLint("HardwareIds")
    get() = android.provider.Settings.Secure.getString(
        contentResolver,
        android.provider.Settings.Secure.ANDROID_ID
    )
