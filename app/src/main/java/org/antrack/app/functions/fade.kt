package org.antrack.app.functions

import android.view.View

fun View.fadeIn(callback: () -> Unit = {}) {
    alpha = 0f
    visibility = View.VISIBLE
    animate()
        .alpha(1f)
        .withEndAction { callback() }
        .start()
}

fun View.fadeOut(callback: () -> Unit = {}) {
    animate()
        .alpha(0f)
        .withEndAction {
            visibility = View.GONE
            callback()
        }
        .start()
}
