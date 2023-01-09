@file:Suppress("OVERRIDE_DEPRECATION")

package org.antrack.app.ui

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import app.BuildConfig
import app.R
import org.antrack.app.*
import org.antrack.app.cloud.Cloud
import org.antrack.app.functions.requestIgnoreBatteryOptimisation
import org.antrack.app.functions.toast
import org.antrack.app.functions.touch
import org.antrack.app.libs.*
import java.io.File

class WizardActivity : PermissionsActivity() {
    private val admin = Admin()
    private val appStatus = AppStatus(this)
    private var pluginChosen = false
    lateinit var closeButton: Button

    companion object {
        private val wizardCompleteFile = App.dataDir + WIZARD_COMPLETE_FILE

        fun launch(activity: Activity, code: Int) {
            val intent = Intent(activity, WizardActivity::class.java)
            activity.startActivityForResult(intent, code)
        }

        fun isNeedToLaunch(): Boolean {
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
                Manifest.permission.READ_SMS
            )
        ) { ok ->
            if (ok) {
                main()
            } else {
                toast(R.string.wizard_nopermissions)
                checkPermissions()
            }
        }
    }

    private fun main() {
        setContentView(R.layout.activity_wizard)
        setupAllFilesAccessButton()
        setupBackgroundWorkButton()
        setupAdminButton()
        setupRootButton()
        setupDropboxButton()
        setupCloseButton()
    }

    private fun setupAllFilesAccessButton() {
        val button = findViewById<Button>(R.id.button_files_access)
        if (Build.VERSION.SDK_INT >= 30) {
            button.setOnClickListener {
                if (!appStatus.haveAccessToAllFiles) {
                    showAllFilesAccessSettingsScreen()
                } else {
                    toast(R.string.already_allowed)
                }
            }
        } else {
            button.visibility = View.GONE
        }
    }

    private fun setupBackgroundWorkButton() {
        val button = findViewById<Button>(R.id.button_background_work)
        button.setOnClickListener {
            if (!appStatus.isIgnoringBatteryOptimizations) {
                requestIgnoreBatteryOptimisation()
            } else {
                toast(R.string.already_allowed)
            }
        }
    }

    private fun setupAdminButton() {
        val button = findViewById<Button>(R.id.button_admin)
        button.setOnClickListener {
            if (!admin.isActive) {
                admin.showDialog(this)
            } else {
                toast(R.string.already_allowed)
            }
        }
    }

    private fun setupRootButton() {
        val button = findViewById<Button>(R.id.button_root)
        if (Shell.checkSu()) {
            button.setOnClickListener {
                if (Shell.checkSuRun()) {
                    toast(R.string.root_rights_granted)
                } else {
                    toast(R.string.no_root_right)
                }
            }
        } else {
            button.isEnabled = false
        }
    }

    private fun setupDropboxButton() {
        val button = findViewById<Button>(R.id.button_dropbox)
        button.setOnClickListener { v ->
            Settings.plugin = "dropbox"
            try {
                pluginChosen = true
                Cloud.auth(this@WizardActivity)
            } catch (e: InterruptedException) {
                toast(R.string.no_internet)
            }
        }
    }

    private fun setupCloseButton() {
        closeButton = findViewById(R.id.button_close)
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

        // Handle Dropbox plugin auth
        if (pluginChosen) {
            val token = Cloud.resume()
            if (token != null) {
                Settings.token = token
                closeButton.isEnabled = true
            } else {
                toast(R.string.authentication_failed)
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.R)
    private fun showAllFilesAccessSettingsScreen() {
        try {
            val intent = Intent(
                android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.parse("package:" + BuildConfig.APPLICATION_ID)
            ).apply {
                // We need CLEAR_TOP flag to remove previous settings screen
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
        } catch (e: Exception) {
            toast(e.message.toString())
        }
    }
}
