@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.antrack.app.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.R
import org.antrack.app.Env
import org.antrack.app.functions.color
import org.antrack.app.functions.plus
import org.antrack.app.functions.readAsList
import java.io.File

class LogsFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Otherwise GetActivity() return null after orientation change
        retainInstance = true

        val view = inflater.inflate(R.layout.fragment_textview, container, false)
        val textView: TextView = view.findViewById(R.id.fragment_textview_text)

        showLoading()
        onFileUpdate(textView)
        return view
    }

    private fun onFileUpdate(textView: TextView) {
        if (!File(Env.logFilePath).exists()) {
            showNoData()
            return
        }

        readLogAndUpdateAsync(textView)
    }

    private fun readLogAndUpdateAsync(textView: TextView) = async {
        try {
            val logs = File(Env.logFilePath).readAsList()
            if (logs.isEmpty()) {
                showNoData()
                return@async
            }

            val logsText = prepareLogs(logs)
            showTextInUiThread(textView, logsText)
        } catch (e: Exception) {
            showException(e)
        }
    }

    private fun prepareLogs(logs: List<String>): SpannableStringBuilder {
        return logs
            .map(::recolor)
            .reversed()
            .map { it + "\n" }
            .reduce { acc, string -> acc + string }
    }

    private fun recolor(text: CharSequence): CharSequence {
        if (text.length < 15) return text

        val dateText = text.subSequence(0, 14).trim()
        val logText = text.subSequence(15, text.length).trim()

        val logText2 = when {
            logText.startsWith("[E]") -> logText.color(Color.RED)
            logText.startsWith("Service started") -> logText.color(Color.CYAN)
            else -> logText
        }

        return dateText.color(Color.WHITE) + " " + logText2
    }

    private fun showTextInUiThread(
        textView: TextView,
        logsText: SpannableStringBuilder
    ) {
        runOnUiThread {
            textView.text = logsText
            textView.movementMethod = ScrollingMovementMethod()
            hideMessage()
        }
    }
}
