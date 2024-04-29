@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.antrack.app.ui.fragments

import android.app.Fragment
import android.os.Bundle
import android.view.View
import android.widget.TextView
import app.R
import org.antrack.app.Env
import org.antrack.app.functions.className
import org.antrack.app.functions.fadeIn
import org.antrack.app.functions.fadeOut
import org.antrack.app.functions.logE
import org.antrack.app.functions.toast
import java.io.File
import java.io.IOException

abstract class BaseFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Just to hide old message on fragment change
        hideMessage()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.fadeIn()
    }

    protected fun async(block: () -> Unit) {
        Env.executor.submit(block)
    }

    protected fun runOnUiThread(block: () -> Unit) {
        activity?.runOnUiThread(block)
    }

    protected fun toast(resId: Int) {
        toast(getString(resId))
    }

    protected fun toast(string: String) {
        runOnUiThread {
            activity?.toast(string)
        }
    }

    protected fun showException(e: Exception) {
        e.printStackTrace()
        logE(className, "Error: ${e.message}")
        toast("Error: ${e.message}")
        showNoData()
    }

    protected fun showLoading() {
        hideMessage()
        showMessage(R.string.loading)
    }

    protected fun showNoData() {
        hideMessage()
        showMessage(R.string.message_nodata)
    }

    protected fun showNoModule() {
        hideMessage()
        showMessage(R.string.message_nomodule)
    }

    fun hideMessage() {
        runOnUiThread {
            activity?.findViewById<TextView>(R.id.message)?.fadeOut()
        }
    }

    private fun showMessage(resId: Int) {
        showMessage(getString(resId))
    }

    private fun showMessage(message: String) {
        runOnUiThread {
            activity?.findViewById<TextView>(R.id.message)?.apply {
                text = message
                fadeIn()
            }
        }
    }

    fun runCommandAsync(cmd: String) {
        try {
            File(Env.ctlFilePath).writeText(cmd)
        } catch (e: IOException) {
            logE("runCommandAsync", "Can't run command $cmd: $e")
        }
    }
}
