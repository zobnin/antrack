package org.antrack.app

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

fun Activity.showKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

fun Activity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (currentFocus != null)
        imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
}

