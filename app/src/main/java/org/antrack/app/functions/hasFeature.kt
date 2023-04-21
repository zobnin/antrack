package org.antrack.app.functions

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager

val Context.hasTelephony: Boolean
    get() = packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)

val Context.hasAnyCamera: Boolean
    get() = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

val Context.hasBackCamera: Boolean
    @SuppressLint("UnsupportedChromeOsCameraSystemFeature")
    get() = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)

val Context.hasFrontCamera: Boolean
    get() = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
