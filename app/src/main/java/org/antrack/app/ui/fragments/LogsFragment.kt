package org.antrack.app.ui.fragments

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.R
import org.antrack.app.libs.Files
import org.antrack.app.libs.L
import org.antrack.app.libs.Utils
import org.antrack.app.ui.State
import org.antrack.app.ui.U
import java.io.File
import java.io.IOException

class LogsFragment : BaseFragment() {
    internal val TAG = "LogsFragment"

    override val module = ""
    val logsFile = "/logs"

    lateinit var textView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Otherwise GetActivity() return null after orientation change
        retainInstance = true

        val view = inflater.inflate(R.layout.fragment_textview, container, false)
        textView = view.findViewById(R.id.fragment_textview_text) as TextView

        onFileUpdate()

        if (!State.device!!.isMain) {
            U.getFileAsync(watchFile!!)
        }

        textView.alpha = 0f
        textView.animate().alpha(1f)

        return view
    }

    override fun onFileUpdate() {
        val path = U.getLocalPath(logsFile)

        if (!File(path).exists()) {
            return
        }

        Thread(Runnable {
            try {
                val logsList = Files.textFileToArray(U.getLocalPath(logsFile))

                if (logsList.isEmpty()) {
                    showNoDataOrLoading()
                    return@Runnable
                }

                val logsText = Utils.arrayToStringReverse(logsList.toTypedArray(), "\n")

                if (activity == null) return@Runnable

                activity.runOnUiThread {
                    textView.text = logsText
                    textView.movementMethod = ScrollingMovementMethod()
                    hideAllMessages()
                }
            } catch (e: IOException) {
                L.e(TAG, "Cat read $logsFile: " + e.toString())
            }
        }).start()
    }
}
