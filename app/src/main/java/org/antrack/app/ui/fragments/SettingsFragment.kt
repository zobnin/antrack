package org.antrack.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.R

class SettingsFragment : BaseFragment() {
    override val module = ""
    override val needSubtitle = false
    override fun onFileUpdate() {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val settingsFragment = SettingsFragmentNested()
        val ft = childFragmentManager.beginTransaction()
        ft.add(R.id.settings_container, settingsFragment).commit()

        view.alpha = 0f
        view.animate().alpha(1f)

        return view
    }
}
