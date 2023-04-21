@file:Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")

package org.antrack.app.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import app.R
import org.antrack.app.Env
import org.antrack.app.RESULT_FILE
import org.antrack.app.functions.*
import org.antrack.app.modules.ModuleInterface
import org.antrack.app.modules.Modules
import org.antrack.app.watcher.FileWatcher
import org.antrack.app.watcher.IWatcherCallback
import java.io.File

class ShellFragment : BaseFragment() {
    private lateinit var resultView: TextView

    private var lastCommand = ""

    inner class FragmentCallback : IWatcherCallback {
        override val watchFile = RESULT_FILE
        override fun onFileUpdated(path: String) {
            onResultFileUpdated()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Otherwise GetActivity() return null after orientation change
        retainInstance = true

        val main = inflater.inflate(R.layout.fragment_shell, container, false)
        val ps1 = main.findViewById(R.id.fragment_shell_ps1) as TextView
        val inputField = main.findViewById(R.id.fragment_shell_edittext) as EditText
        resultView = main.findViewById(R.id.fragment_shell_textview) as TextView

        ps1.text = Env.deviceName + "$ "
        inputField.requestFocus()

        setListeners(main, resultView, inputField)

        return main
    }

    override fun onStart() {
        super.onStart()
        FileWatcher.addCallback(className, FragmentCallback())
    }

    override fun onStop() {
        super.onStop()
        resultView.hideKeyboard()
        FileWatcher.removeCallback(className)
    }

    private fun setListeners(
        mainView: View,
        resultView: TextView,
        inputField: EditText,
    ) {
        mainView.setOnClickListener {
            inputField.focusAndShowKeyboard()
        }

        resultView.setOnClickListener {
            inputField.focusAndShowKeyboard()
        }

        inputField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                onEnterPressed(inputField)
            }
            true
        }
    }

    private fun onEnterPressed(inputField: EditText) {
        lastCommand = inputField.text.toString()
        inputField.setText("")

        if (lastCommand.isNotBlank()) {
            runCommandAsync(lastCommand)
            resultView.setText(R.string.loading)
        }
    }

    private fun onResultFileUpdated() = async {
        try {
            val module = Modules.get().getOrElse(lastCommand.split(" ")[0]) { null }

            val out = when {
                module != null && module.resultType() == "txt" -> readModuleOutFile(module)
                else -> readResultFile()
            }

            insertOutInResultViewOnUiThread(out)
        } catch (e: Exception) {
            logE(className, "Can't read result file: $e")
            activity.toast(e.message.toString())
        }
    }

    private fun insertOutInResultViewOnUiThread(out: String) {
        activity.runOnUiThread {
            resultView.text = out
            resultView.fadeIn()
        }
    }

    private fun readResultFile() = File(Env.resultFilePath).readText()

    private fun readModuleOutFile(module: ModuleInterface): String {
        val resultFile = File(Env.mainDirPath + module.result())

        return if (resultFile.isDirectory) {
            resultFile.listFiles()?.joinToString("\n") { it.readText() } ?: "error"
        } else {
            resultFile.readText()
        }
    }
}