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

class CallDialog {
    public static void show(final Activity activity, String number) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        // FIXME translate
        builder.setTitle("Make call");

        int p = getDpInPixels(activity, 20);

        LinearLayout linear = new LinearLayout(activity);
        linear.setOrientation(LinearLayout.VERTICAL);
        linear.setPadding(p,p,p,p);

        final TextView textNumber = new TextView(activity);
        // FIXME translate
        textNumber.setText("Number:");

        final EditText editNumber = new EditText(activity);
        editNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (number != null) {
            editNumber.setText(number);
        }

        linear.addView(textNumber);
        linear.addView(editNumber);
        builder.setView(linear);

        // FIXME translate
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String number = editNumber.getText().toString();

                if (number.equals("")) {
                    // FIXME translate
                    Utils.showToast(activity, "Fill number field");
                    return;
                }

                number = number.replace(" ", "");
                number = number.replace("-", "");
                U.runCommandAsync("call " + number);

                dialog.dismiss();
            }
        });

        // FIXME translate
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
