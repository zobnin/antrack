@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.antrack.app.ui.fragments

import android.text.Spannable
import org.antrack.app.functions.bold
import org.antrack.app.functions.className
import org.antrack.app.functions.logD
import org.antrack.app.functions.plus
import org.antrack.app.modules.Module
import org.antrack.app.modules.ModulesSerializer

class ModulesFragment : ListBaseFragment() {
    private val modSerializer = ModulesSerializer()

    override fun onStart() {
        super.onStart()
        showLoadingIfAdapterEmpty()
        readModulesAndUpdateAsync()
    }

    private fun readModulesAndUpdateAsync() = async {
        try {
            val modules = modSerializer.read().values
            val strings = modules.map { moduleToSpannable(it) }
            showListInUiThread(strings)
            logD(className, "Fragment updated")
        } catch (e: Exception) {
            showException(e)
        }
    }

    private fun moduleToSpannable(module: Module): Spannable {
        return "\n".bold() +
                module.name.bold() + "\n" +
                module.desc + "\n" +
                "Version: " + module.version + "\n" +
                "Author: " + module.author + "\n"
    }
}
