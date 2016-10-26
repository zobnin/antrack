package org.antrack.app.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import org.antrack.app.libs.Utils;
import org.antrack.app.ui.U;

import app.R;

class SendSmsDialog {
    public static void show(final Activity activity, String number, String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.send_sms);

        int p = getDpInPixels(activity, 20);

        LinearLayout linear = new LinearLayout(activity);
        linear.setOrientation(LinearLayout.VERTICAL);
        linear.setPadding(p,p,p,p);

        final TextView textNumber = new TextView(activity);
        textNumber.setText(R.string.make_call_number);

        final EditText editNumber = new EditText(activity);
        editNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (number != null) {
            editNumber.setText(number);
        }

        final TextView textText = new TextView(activity);
        textText.setText(R.string.send_sms_text);

        final Space space = new Space(activity);
        space.setMinimumHeight(p);

        final EditText editText = new EditText(activity);
        if (text != null) {
            editText.setText(text);
        }

        if (number != null) {
            editText.requestFocus();
        }

        linear.addView(textNumber);
        linear.addView(editNumber);
        linear.addView(space);
        linear.addView(textText);
        linear.addView(editText);
        builder.setView(linear);

        // FIXME translate
        builder.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String number = editNumber.getText().toString();
                String text = editText.getText().toString();

                if (number.equals("") || text.equals("")) {
                    Utils.showToast(activity, activity.getResources().getString(R.string.message_fill_number_and_text));
                    return;
                }

                number = number.replace(" ", "");
                number = number.replace("-", "");

                U.runCommandAsync(Mod.SMS + number + " " + text);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private static int getDpInPixels(Context context, int dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
