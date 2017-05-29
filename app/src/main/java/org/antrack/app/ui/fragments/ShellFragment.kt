package org.antrack.app.ui.fragments

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import app.R
import org.antrack.app.libs.Files
import org.antrack.app.libs.L
import org.antrack.app.showKeyboard
import org.antrack.app.ui.State
import org.antrack.app.ui.U
import java.io.IOException

class ShellFragment : BaseFragment() {
    private val TAG = "ShellFragment"

    override val module = "cmd"

    lateinit var editText: EditText
    lateinit var textView: TextView
    lateinit var ps1: TextView

    internal var progressThread: Thread? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Otherwise GetActivity() return null after orientation change
        retainInstance = true

        checkModule() || return null

        val view = inflater.inflate(R.layout.fragment_shell, container, false)
        view.alpha = 0f
        view.animate().alpha(1f)

        // Show keyboard when user clicks on any place
        view.setOnClickListener { activity.showKeyboard() }

        ps1 = view.findViewById(R.id.fragment_shell_ps1) as TextView
        val text = State.device!!.name + "$ "
        ps1.text = text

        editText = view.findViewById(R.id.fragment_shell_edittext) as EditText
        editText.requestFocus()

        textView = view.findViewById(R.id.fragment_shell_textview) as TextView
        textView.movementMethod = ScrollingMovementMethod()
        textView.setOnClickListener { activity.showKeyboard() }

        editText.setOnEditorActionListener { _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendCommand(editText.text.toString())

                editText.setText("")
                textView.text = ""

                startProgress()

                handled = true
            }
            handled
        }

        return view
    }

    private fun sendCommand(cmd: String) {
        U.runCommandAsync(command + " " + cmd)
    }

    private fun addText(text: String) {
        val activity = activity ?: return

        activity.runOnUiThread { textView.append(text) }
    }

    private fun startProgress() {
        if (State.device!!.isMain)
            return

        stopProgress()

        progressThread = Thread(Runnable {
            var seconds = 0

            while (true) {
                addText(".")

                if (seconds == 60) {
                    addText(" :(")
                }

                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    return@Runnable
                }

                seconds += 1
            }
        })
        progressThread!!.start()
    }

    private fun stopProgress() {
        if (State.device!!.isMain)
            return

        if (progressThread != null && progressThread!!.isAlive) {
            progressThread!!.interrupt()
        }
    }

    override fun onFileUpdate() {
        try {
            val out = Files.readTextFile(U.getLocalPath(watchFile!!))

            activity.runOnUiThread {
                stopProgress()
                textView.alpha = 0f
                // Remove time stamp
                textView.text = out.substring(out.indexOf('\n') + 1)
                textView.animate().alpha(1f)
            }
        } catch (e: IOException) {
            L.e(TAG, "Can't read cmdout: " + e)
        }

    }
}
