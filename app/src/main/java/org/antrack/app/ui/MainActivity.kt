@file:Suppress("DEPRECATION")

package org.antrack.app.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import app.R
import org.antrack.app.*
import org.antrack.app.functions.className
import org.antrack.app.functions.color
import org.antrack.app.functions.logD
import org.antrack.app.service.CloudService
import org.antrack.app.ui.fragments.*
import java.util.*

class MainActivity : Activity() {

    private var initDone = false

    private val fragments by lazy {
        listOf(
            InfoFragment(),
            ModulesFragment(),
            LogsFragment(),
            ShellFragment(),
            SettingsFragment(),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (WizardActivity.needToLaunch()) {
            WizardActivity.launch(this, WIZARD_FIRST_LAUNCH_CODE)
        } else {
            main()
        }
    }

    private fun main() {
        logD(className, "Running on: " + Build.BRAND + " " + Build.MODEL)
        CloudService.start(this)
        setContentView(R.layout.activity_main)
        initNavButtons()
        initDefaultFragment()
        initDone = true
    }

    private fun initNavButtons() {
        val buttons = listOf<TextView>(
            findViewById(R.id.status_tv),
            findViewById(R.id.modules_tv),
            findViewById(R.id.logs_tv),
            findViewById(R.id.shell_tv),
            findViewById(R.id.settings_tv),
        )

        highlightButton(buttons, Settings.fragmentId)

        buttons.forEachIndexed { idx, button ->
            setBottomButtonClickListener(buttons, button, idx)
        }
    }

    private fun setBottomButtonClickListener(
        buttons: List<TextView>,
        button: TextView,
        idx: Int,
    ) {
        button.setOnClickListener {
            loadFragment(fragments[idx])
            highlightButton(buttons, idx)
            Settings.fragmentId = idx
        }
    }

    private fun highlightButton(
        buttons: List<TextView>,
        idx: Int
    ) {
        // Reset styles for all buttons
        buttons.forEach { it.text = it.text.toString() }
        // Highlight current button
        buttons[idx].text = buttons[idx].text.color(getColor(R.color.accent))
    }

    private fun initDefaultFragment() {
        if (!initDone) {
            loadFragment(fragments[Settings.fragmentId])
        }
    }

    private fun loadFragment(fragment: BaseFragment) {
        fragmentManager.beginTransaction().apply {
            replace(R.id.container, fragment, "fragment")
            commitAllowingStateLoss()
        }

        logD(className, "Fragment loaded")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == WIZARD_FIRST_LAUNCH_CODE && data != null) {
            main()
        }
    }
}