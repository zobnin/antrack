package org.antrack.app.ui

import app.R
import org.antrack.app.C
import org.antrack.app.FileWatcher
import org.antrack.app.libs.LoadingDialog
import org.antrack.app.libs.Utils
import org.antrack.app.ui.callbacks.FeaturesCallback
import org.antrack.app.ui.callbacks.KeyCallback
import org.antrack.app.ui.callbacks.ModulesCallback
import org.antrack.app.ui.callbacks.OsidCallback
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import java.io.File

class WaitFilesAndSwitchDevice {
    init {
        val modulesFile = U.getLocalPath(C.MODULES_FILE)
        val featuresFile = U.getLocalPath(C.FEATURES_FILE)
        val osidFile = U.getLocalPath(C.OSID_FILE)
        val keyFile = U.getLocalPath(C.PUBLIC_KEY_FILE)

        var isWaiting = false

        if (!File(modulesFile).exists()) {
            FileWatcher.addCallback("modules", ModulesCallback(MainActivity.act!!))
            if (!State.device.isMain) {
                U.getFileAsync(C.MODULES_FILE)
            }
            isWaiting = true
        }

        if (!File(featuresFile).exists()) {
            FileWatcher.addCallback("features", FeaturesCallback(MainActivity.act!!))
            if (!State.device.isMain) {
                U.getFileAsync(C.FEATURES_FILE)
            }
            isWaiting = true
        }

        if (!File(osidFile).exists()) {
            FileWatcher.addCallback("osid", OsidCallback(MainActivity.act!!))
            if (!State.device.isMain) {
                U.getFileAsync(C.OSID_FILE)
            }
            isWaiting = true
        }

        if (!File(keyFile).exists()) {
            FileWatcher.addCallback("key", KeyCallback(MainActivity.act!!))
            if (!State.device.isMain) {
                U.getFileAsync(C.PUBLIC_KEY_FILE)
            }
            isWaiting = true
        }

        if (isWaiting) {
            LoadingDialog.show(MainActivity.act!!, MainActivity.act!!.resources.getString(R.string.loading_dialog))

            // Workaround to stop loading dialog if don't get modules
            doAsync {
                Utils.sleep(15)
                if (LoadingDialog.isShown) {
                    LoadingDialog.hide(MainActivity.act!!)
                    MainActivity.act!!.toast(R.string.cant_connect)
                }
            }
        } else {
            MainActivity.act!!.switchDevice(true)
        }
    }
}

