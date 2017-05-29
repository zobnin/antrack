package org.antrack.app.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import app.R
import org.antrack.app.libs.Utils
import org.antrack.app.ui.U

internal object CallDialog {
    fun show(activity: Activity, number: String?) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.make_call_title)

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

        linear.addView(textNumber)
        linear.addView(editNumber)
        builder.setView(linear)

        builder.setPositiveButton(R.string.make_call_title, DialogInterface.OnClickListener { dialog, which ->
            var number = editNumber.text.toString()

            if (number == "") {
                Utils.showToast(activity, activity.resources.getString(R.string.make_call_error))
                return@OnClickListener
            }

            number = number.replace(" ", "")
            number = number.replace("-", "")
            // FIXME
            U.runCommandAsync("dial " + number)

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
