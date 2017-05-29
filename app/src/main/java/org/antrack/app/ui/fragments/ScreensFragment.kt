package org.antrack.app.ui.fragments

import android.os.Bundle
import android.view.*
import app.R
import org.antrack.app.ui.U

class ScreensFragment : ImagesFragment() {
    override val module = Mod.SCREENSHOT

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        checkRoot() || return null

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_screens, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.toolbar_action_front_camera -> {
                if (checkModule()) {
                    U.runCommandAsync(module)
                } else {
                    showNoModuleToast()
                }
                return true
            }
            R.id.toolbar_action_delete -> {
                showRemoveDialog()
                return true
            }
        }
        return false
    }
}
