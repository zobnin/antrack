@file:Suppress("OVERRIDE_DEPRECATION")

package org.antrack.app.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import app.R
import org.antrack.app.App
import org.antrack.app.Settings
import org.antrack.app.WIZARD_COMPLETE_FILE
import org.antrack.app.cloud.Cloud
import org.antrack.app.functions.requestIgnoreBatteryOptimisation
import org.antrack.app.functions.toast
import org.antrack.app.functions.touch
import org.antrack.app.libs.Admin
import org.antrack.app.libs.Shell
import java.io.File

class WizardActivity : PermissionsActivity() {
    private val admin = Admin()
    private val appStatus = AppStatus(this)
    private var pluginChosen = false

    private val filesAccessButton: Button
        get() = findViewById(R.id.button_files_access)

    private val backgroundButton: Button
        get() = findViewById(R.id.button_background_work)

    private val adminButton: Button
        get() = findViewById(R.id.button_admin)

    private val rootButton: Button
        get() = findViewById(R.id.button_root)

    private val dropboxButton: Button
        get() = findViewById(R.id.button_dropbox)

    private val closeButton: Button
        get() = findViewById(R.id.button_close)

    companion object {
        private val wizardCompleteFile = App.dataDir + WIZARD_COMPLETE_FILE

        fun launch(activity: Activity, code: Int) {
            val intent = Intent(activity, WizardActivity::class.java)
            activity.startActivityForResult(intent, code)
        }

        fun needToLaunch(): Boolean {
            return !File(wizardCompleteFile).exists()
        }

        fun wizardComplete() {
            File(wizardCompleteFile).touch()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
    }

    private fun checkPermissions() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.SEND_SMS,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.PROCESS_OUTGOING_CALLS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_SMS,
                Manifest.permission.POST_NOTIFICATIONS,
            )
        ) { granted ->
            if (granted) {
                main()
            } else {
                toast(R.string.wizard_nopermissions)
                checkPermissions()
            }
        }
    }

    private fun main() {
        setContentView(R.layout.activity_wizard)
        setupFilesAccessButton()
        setupBackgroundWorkButton()
        setupAdminButton()
        setupRootButton()
        setupDropboxButton()
        setupCloseButton()
    }

    private fun setupFilesAccessButton() {
        if (Build.VERSION.SDK_INT < 30) {
            filesAccessButton.visibility = View.GONE
            return
        }

        filesAccessButton.setOnClickListener {
            if (!appStatus.haveAccessToAllFiles) {
                showAllFilesAccessSettingsScreen()
            } else {
                toast(R.string.already_allowed)
            }
        }
    }

    private fun setupBackgroundWorkButton() {
        backgroundButton.setOnClickListener {
            if (!appStatus.isIgnoringBatteryOptimizations) {
                requestIgnoreBatteryOptimisation()
            } else {
                toast(R.string.already_allowed)
            }
        }
    }

    private fun setupAdminButton() {
        adminButton.setOnClickListener {
            if (!admin.isActive) {
                admin.showDialog(this)
            } else {
                toast(R.string.already_allowed)
            }
        }
    }

    private fun setupRootButton() {
        if (!Shell.checkSu()) {
            rootButton.isEnabled = false
            return
        }

        rootButton.setOnClickListener {
            if (Shell.checkSuRun()) {
                toast(R.string.root_rights_granted)
            } else {
                toast(R.string.no_root_right)
            }
        }
    }

    private fun setupDropboxButton() {
        dropboxButton.setOnClickListener { v ->
            Settings.plugin = Cloud.DROPBOX

            try {
                pluginChosen = true
                Cloud.auth(this@WizardActivity, Settings.plugin)
            } catch (e: InterruptedException) {
                toast(R.string.no_internet)
            }
        }
    }

    private fun setupCloseButton() {
        closeButton.isEnabled = Settings.token.isNotEmpty()
        closeButton.setOnClickListener { exit() }
    }

    override fun onBackPressed() {
        if (Settings.token.isEmpty()) {
            toast(R.string.authentication_required)
        } else {
            exit()
        }
    }

    private fun exit() {
        wizardComplete()
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onResume() {
        super.onResume()

        if (pluginChosen) {
            processToken(Cloud.resume())
        }
    }

    private fun processToken(token: String?) {
        if (token != null) {
            Settings.token = token
            closeButton.isEnabled = true
        } else {
            toast(R.string.authentication_failed)
        }
    }
}
