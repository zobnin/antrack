package org.antrack.app.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import app.R
import org.antrack.app.libs.Utils
import org.antrack.app.ui.U

internal object SendSmsDialog {
    fun show(activity: Activity, number: String?, text: String?) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.send_sms)

        val p = getDpInPixels(activity, 20)

        val linear = LinearLayout(activity)
        linear.orientation = LinearLayout.VERTICAL
        linear.setPadding(p, p, p, p)

        val textNumber = TextView(activity)
        textNumber.setText(R.string.make_call_number)

        val editNumber = EditText(activity)
        editNumber.inputType = InputType.TYPE_CLASS_NUMBER
        if (number != null) {
            editNumber.setText(number)
        }

        val textText = TextView(activity)
        textText.setText(R.string.send_sms_text)

        val space = Space(activity)
        space.minimumHeight = p

        val editText = EditText(activity)
        if (text != null) {
            editText.setText(text)
        }

        if (number != null) {
            editText.requestFocus()
        }

        linear.addView(textNumber)
        linear.addView(editNumber)
        linear.addView(space)
        linear.addView(textText)
        linear.addView(editText)
        builder.setView(linear)

        // FIXME translate
        builder.setPositiveButton(R.string.send, DialogInterface.OnClickListener { dialog, which ->
            var number = editNumber.text.toString()
            val text = editText.text.toString()

            if (number == "" || text == "") {
                Utils.showToast(activity, activity.resources.getString(R.string.message_fill_number_and_text))
                return@OnClickListener
            }

            number = number.replace(" ", "")
            number = number.replace("-", "")

            U.runCommandAsync(Mod.SMS + number + " " + text)
            dialog.dismiss()
        })

        builder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.dismiss() }

        builder.show()
    }

    private fun getDpInPixels(context: Context, dp: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}
