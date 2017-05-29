package org.antrack.app.ui.fragments

import android.app.Activity
import org.antrack.app.libs.Utils
import org.antrack.app.ui.State

object ModUtils {
    fun getFile(mod: String): String? {
        return State.device.modules[mod]!!.result
    }

    fun checkModule(mod: String): Boolean {
        if (!State.device.modules.containsKey(mod)) {
            return false
        }
        return true
    }

    fun checkPhone(): Boolean {
        if (!State.device.features.phone) {
            return false
        }
        return true
    }

    fun showNoModuleToast(activity: Activity, module: String) {
        activity.runOnUiThread {
            // FIXME translate
            Utils.showToast(activity, "Module not found: " + module)
        }
    }
}

