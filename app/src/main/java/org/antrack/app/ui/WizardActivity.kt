package org.antrack.app.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast
import app.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import org.antrack.app.App
import org.antrack.app.C
import org.antrack.app.Pw
import org.antrack.app.Settings
import org.antrack.app.libs.Battery
import org.antrack.app.libs.Files
import org.antrack.app.libs.Shell
import org.antrack.app.libs.Utils
import java.io.File

class WizardActivity : AppCompatActivity() {
    internal var pluginChosen = false

    //lateinit var aTools: Admin
    lateinit var closeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
    }

    private fun checkPermissions() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
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
                ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                if (report.areAllPermissionsGranted()) {
                    if (Build.VERSION.SDK_INT >= 23 && !Battery.isIgnoringBatteryOptimizations(this@WizardActivity)) {
                        //Battery.requestIgnoreBatteryOptimisation(WizardActivity.this);
                        AlertDialog.Builder(this@WizardActivity)
                                .setTitle(R.string.battery_alert_title)
                                .setMessage(R.string.battery_alert_message)
                                .setPositiveButton(android.R.string.yes) { dialogInterface, i -> Battery.openBatteryOptimizationSettings(this@WizardActivity) }
                                .show()
                    }
                    main()
                } else {
                    Toast.makeText(this@WizardActivity,
                            R.string.wizard_nopermissions, Toast.LENGTH_SHORT).show()
                    checkPermissions()
                }
            }

            override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                token.continuePermissionRequest()
            }
        }).check()
    }

    private fun main() {
        val context = this

        setContentView(R.layout.activity_wizard)

        /* TODO
        val button_admin = findViewById(R.id.button_admin) as Button
        button_admin.setOnClickListener {
            aTools = Admin(this@WizardActivity)
            aTools.showDialog(this@WizardActivity)
        }
        */

        val rootButton = findViewById(R.id.button_root) as Button
        if (Shell.checkSu()) {
            rootButton.setOnClickListener {
                if (Shell.checkSuRun()) {
                    Settings.put(C.S_USE_ROOT, C.TRUE)
                    Utils.showToast(context, resources.getString(R.string.root_rights_granted))
                } else {
                    Utils.showToast(context, "No root rights")
                }
            }
        } else {
            rootButton.isEnabled = false
        }

        val dropboxButton = findViewById(R.id.button_dropbox) as Button
        dropboxButton.setOnClickListener { v ->
            Settings.put(C.S_PLUGIN, "dropbox")
            try {
                pluginChosen = true
                Pw.auth(this@WizardActivity)
            } catch (e: InterruptedException) {
                Utils.showToast(this@WizardActivity, "No internet, try later")
            }
        }

        closeButton = findViewById(R.id.button_close) as Button
        closeButton.isEnabled = false
        closeButton.setOnClickListener { exit() }
    }

    private fun exit() {
        wizardComplete()
        // Pw is singleton and will be used by service and activity
        Pw.connect()

        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onBackPressed() {
        if (Settings.readToken().isNullOrEmpty()) {
            // FIXME translate
            Utils.showToast(this@WizardActivity, "Authentication required")
        } else {
            wizardComplete()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        // Handle Dropbox plugin auth
        if (pluginChosen) {
            val token = Pw.resume()
            if (token != null) {
                Settings.saveToken(token)
                closeButton.isEnabled = true
            } else {
                // FIXME здесь надо как-то обрабатывать ситуацию если нет токена
            }
        }
    }

    companion object {
        fun needLaunchWizard(): Boolean {
            val wizardCompleteFile = App.context!!
                    .applicationInfo.dataDir + C.WIZARD_COMPLETE_FILE
            return !File(wizardCompleteFile).exists()
        }

        fun wizardComplete() {
            val wizardCompleteFile = App.context!!
                    .applicationInfo.dataDir + C.WIZARD_COMPLETE_FILE
            Files.touch(wizardCompleteFile)
        }
    }
}
