package org.antrack.app.ui.fragments;

import android.app.Activity;

import org.antrack.app.libs.Utils;
import org.antrack.app.ui.V;

class Mod {
    static final String APPS = "apps";
    static final String AUDIO = "audio";
    static final String CAMERA = "camera";
    static final String CMD = "cmd";
    static final String CONTACTS = "contacts";
    static final String DIAL = "dial";
    static final String DUMPSMS = "dumpsms";
    static final String HIDE = "hide";
    static final String INFO = "info";
    static final String LOCATE = "locate";
    static final String LOCK = "lock";
    static final String LOGCALLS = "logcalls";
    static final String NOTIFY = "notify";
    static final String SCREENONPHOTO = "screenonphoto";
    static final String SCREENSHOT = "screenshot";
    static final String SMS = "sms";
    static final String STARTAPP = "startapp";
    static final String STATUS = "status";
    static final String UPLOAD = "upload";
    static final String WIPE = "wipe";
    static final String WIPESD = "wipesd";

    static boolean check(String module) {
        return V.modules.containsKey(module);
    }

    static String getCommand(String module) {
        return V.modules.get(module).command;
    }

    static String getFile(String module) {
        return V.modules.get(module).result;
    }

    static void showNoModule(final Activity activity, final String modName) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // FIXME translate
                Utils.showToast(activity, "Module not found: " + modName);
            }
        });
    }
}
