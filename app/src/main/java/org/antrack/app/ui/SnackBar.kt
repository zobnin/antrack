package org.antrack.app.ui

import android.app.Activity
import android.graphics.Color
import android.support.design.widget.Snackbar
import android.widget.FrameLayout
import android.widget.TextView
import app.R
import org.jetbrains.anko.find

object SnackBar {
    private var snackbar: Snackbar? = null

    fun show(act: Activity, message: String) {
        hide()

        val container = act.find<FrameLayout>(R.id.container)
        snackbar = Snackbar.make(container, message, Snackbar.LENGTH_INDEFINITE)

        val sbView = snackbar!!.view
        val textView = sbView.findViewById(android.support.design.R.id.snackbar_text) as TextView
        textView.setTextColor(Color.CYAN)

        snackbar!!.show()
    }

    fun hide() {
        if (snackbar != null && snackbar!!.isShown) {
            snackbar!!.dismiss()
        }
    }
}

