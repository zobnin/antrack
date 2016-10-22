package org.antrack.app.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.antrack.app.libs.Utils;
import org.antrack.app.ui.U;

import app.R;

class CallDialog {
    public static void show(final Activity activity, String number) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.make_call_title);

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

        linear.addView(textNumber);
        linear.addView(editNumber);
        builder.setView(linear);

        builder.setPositiveButton(R.string.make_call_title, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String number = editNumber.getText().toString();

                if (number.equals("")) {
                    Utils.showToast(activity, activity.getResources().getString(R.string.make_call_error));
                    return;
                }

                number = number.replace(" ", "");
                number = number.replace("-", "");
                U.runCommandAsync("call " + number);

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
