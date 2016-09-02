package org.antrack.app.libs;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

public class Keyboard {
    public static void show(Activity act) {
        InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
    }

    public static void hide(Activity act) {
        InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (act.getCurrentFocus() != null)
            imm.hideSoftInputFromWindow(act.getCurrentFocus().getWindowToken(), 0);
    }
}
