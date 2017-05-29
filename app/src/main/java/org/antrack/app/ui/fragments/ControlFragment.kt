package org.antrack.app.ui.fragments

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Switch
import app.R
import org.antrack.app.C
import org.antrack.app.Init
import org.antrack.app.libs.L
import org.antrack.app.ui.State
import org.antrack.app.ui.U
import java.io.FileInputStream
import java.util.*

class ControlFragment : BaseFragment() {
    internal val TAG = "ControlFragment"

    override val module = "control"

    lateinit var hideSwitch: Switch
    lateinit var systemSwitch: Switch
    lateinit var lostSwitch: Switch

    lateinit var wipeButton: Button
    lateinit var lockButton: Button
    lateinit var alarmButton: Button
    lateinit var smsButton: Button
    lateinit var callButton: Button

    override var watchFile: String? = C.SETTINGS_FILE

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Otherwise GetActivity() return null after orientation change
        retainInstance = true

        val view = inflater.inflate(R.layout.fragment_control, container, false)

        /*** Hide switch  */

        hideSwitch = view.findViewById(R.id.fragment_control_hide) as Switch
        hideSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                showHideIconWarning()
            } else {
                U.runCommandAsync("hide off")
            }
        }

        if (!checkModule("hide")) {
            hideSwitch.isEnabled = false
        }

        /*** System switch  */

        // TODO
        systemSwitch = view.findViewById(R.id.fragment_control_system) as Switch
        systemSwitch.isEnabled = false

        /*** Lost switch  */

        lostSwitch = view.findViewById(R.id.fragment_control_lost) as Switch
        lostSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                U.runCommandAsync("lost on")
                U.banDevice(State.device!!.dir)
            } else {
                U.runCommandAsync("lost off")
            }
        }

        /*** Wipe button  */

        // TODO проверка прав админа

        wipeButton = view.findViewById(R.id.fragment_control_wipe) as Button
        wipeButton.setOnClickListener { showWipeWarning() }

        if (!checkModule("wipe")) {
            wipeButton.isEnabled = false
        }

        /*** Lock button  */

        // TODO проверка прав админа

        lockButton = view.findViewById(R.id.fragment_control_lock) as Button
        lockButton.setOnClickListener { showLockWarning() }

        if (checkModule("lock")) {
            lockButton.isEnabled = false
        }

        /*** Alarm button  */

        alarmButton = view.findViewById(R.id.fragment_control_alarm) as Button
        alarmButton.setOnClickListener {
            // FIXME
            U.runCommandAsync("alarm " + Init.APP_DIR + "/" + C.ALARM_ASSET)
        }

        if (checkModule("alarm")) {
            alarmButton.isEnabled = false
        }

        /*** SMS / Calls  */

        smsButton = view.findViewById(R.id.fragment_control_sms) as Button
        smsButton.setOnClickListener { SendSmsDialog.show(activity, null, null) }

        callButton = view.findViewById(R.id.fragment_control_call) as Button
        callButton.setOnClickListener { CallDialog.show(activity, null) }

        onFileUpdate()

        if (!State.device!!.isMain) {
            U.getFileAsync(watchFile!!)
        }

        return view
    }

    override fun onFileUpdate() {
        Thread(Runnable {
            try {
                val prop = Properties()
                prop.load(FileInputStream(
                        Init.DEVICES_DIR +
                                State.device!!.dir + watchFile))

                val hidden = prop.getProperty(C.S_HIDDEN)
                if (hidden != null && hidden == "true") {
                    activity.runOnUiThread { hideSwitch.isChecked = true }
                } else {
                    activity.runOnUiThread { hideSwitch.isChecked = false }
                }

                val system = prop.getProperty(C.S_SYSTEM_APP)
                if (system != null && system == "true") {
                    activity.runOnUiThread { systemSwitch.isChecked = true }
                } else {
                    activity.runOnUiThread { systemSwitch.isChecked = false }
                }

                val lost = prop.getProperty(C.S_LOST)
                if (lost != null && lost == "true") {
                    activity.runOnUiThread { lostSwitch.isChecked = true }
                } else {
                    activity.runOnUiThread { lostSwitch.isChecked = false }
                }
            } catch (e: Exception) {
                L.e(TAG, e.toString())
            }
        }).start()
    }

    protected fun showHideIconWarning() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.warning)
        builder.setMessage(R.string.hide_icon_warning)

        builder.setPositiveButton(R.string.yes) { dialog, which ->
            // FIXME
            U.runCommandAsync("hide on")
            dialog.dismiss()
        }

        builder.setNegativeButton(R.string.no) { dialog, _ ->
            hideSwitch.isChecked = false
            dialog.dismiss()
        }

        builder.show()
    }

    private fun showWipeWarning() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.warning)

        val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = inflater.inflate(R.layout.dialog_wipe, null, false)

        builder.setView(v)

        builder.setPositiveButton(R.string.yes) { dialog, _ ->
            val checkBox = v.findViewById(R.id.checkbox) as CheckBox
            if (checkBox.isChecked) {
                //U.runCommandAsync(
                //        Mod.runCommand(Mod.WIPESD) + "; " +
                //        Mod.runCommand(Mod.WIPE));
            } else {
                //U.runCommandAsync(Mod.runCommand(Mod.WIPE));
            }
            dialog.dismiss()
        }

        builder.setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }

        builder.show()
    }

    private fun showLockWarning() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.warning)

        val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = inflater.inflate(R.layout.dialog_lock, null, false)

        builder.setView(v)

        builder.setPositiveButton(R.string.yes) { dialog, _ ->
            val editText = v.findViewById(R.id.editText) as EditText
            U.runCommandAsync("lock " + editText.text)
            dialog.dismiss()
        }

        builder.setNegativeButton(R.string.no) { dialog, which -> dialog.dismiss() }

        builder.show()
    }
}
