package org.antrack.app.ui.fragments

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import app.R
import org.antrack.app.ui.U

class PhotosFragment : ImagesFragment() {
    override val module = Mod.CAMERA

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_photos, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.toolbar_action_back_camera -> {
                if (checkModule()) {
                    U.runCommandAsync(module + " back")
                } else {
                    showNoModuleToast()
                }
                return true
            }
            R.id.toolbar_action_front_camera -> {
                if (checkModule()) {
                    U.runCommandAsync(module + " front")
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
