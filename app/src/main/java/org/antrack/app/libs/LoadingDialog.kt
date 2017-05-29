package org.antrack.app.libs

import android.app.Activity
import android.app.ProgressDialog

object LoadingDialog {
    lateinit var loadingDialog: ProgressDialog

    var isShown: Boolean = false

    fun show(act: Activity, message: String) {
        act.runOnUiThread {
            loadingDialog = ProgressDialog(act)
            loadingDialog.setMessage(message)
            loadingDialog.setCancelable(false)
            loadingDialog.setInverseBackgroundForced(false)
            loadingDialog.show()
            isShown = true
        }
    }

    fun hide(act: Activity) {
        act.runOnUiThread {
            if (isShown) {
                loadingDialog.hide()
                isShown = false
            }
        }
    }
}
