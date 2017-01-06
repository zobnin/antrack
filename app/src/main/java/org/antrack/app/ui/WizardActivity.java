package org.antrack.app.ui;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.antrack.app.C;
import org.antrack.app.Init;
import org.antrack.app.Pw;
import org.antrack.app.Settings;
import org.antrack.app.libs.Admin;
import org.antrack.app.libs.Battery;
import org.antrack.app.libs.Shell;
import org.antrack.app.libs.Utils;

import java.util.List;

import app.R;

public class WizardActivity extends AppCompatActivity {
    boolean pluginChosen = false;

    private Admin aTools;
    private Pw pw;

    private Button closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                ).withListener(new MultiplePermissionsListener() {
            @Override public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {
                    // FIXME API
                    Battery.requestIgnoreBatteryOptimisation(WizardActivity.this);
                    main();
                }
            }
            @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
        }).check();
    }

    private void main() {
        final Context context = this;
        setContentView(R.layout.activity_wizard);

        Init.all(this);

        final Button button_admin = (Button) findViewById(R.id.button_admin);
        button_admin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                aTools = new Admin(WizardActivity.this);
                aTools.showDialog(WizardActivity.this);
            }
        });


        final Button rootButton = (Button) findViewById(R.id.button_root);
        if (Shell.checkSu()) {
            rootButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (Shell.checkSuRun()) {
                        Settings.put(C.S_USE_ROOT, C.TRUE);
                        Utils.showToast(context, getResources().getString(R.string.root_rights_granted));
                    } else {
                        Utils.showToast(context, "No root rights");
                    }
                }
            });
        } else {
            rootButton.setEnabled(false);
        }

        final Button dropboxButton = (Button) findViewById(R.id.button_dropbox);
        dropboxButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Settings.put(C.S_PLUGIN, "dropbox");
                try {
                    pluginChosen = true;
                    pw = Pw.getInstance();
                    pw.auth(WizardActivity.this);
                } catch (InterruptedException e) {
                    Utils.showToast(WizardActivity.this, "No internet, try later");
                }
            }
        });

        closeButton = (Button) findViewById(R.id.button_close);
        closeButton.setEnabled(false);
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                exit();
            }
        });
    }

    private void exit() {
        Settings.wizardComplete(this);
        // Pw is singleton and will be used by service and activity
        pw.connect();
        finish();
    }

    @Override
    public void onBackPressed() {
        if (Settings.get(C.S_TOKEN) == null) {
            // FIXME translate
            Utils.showToast(WizardActivity.this, "Authentication required");
        } else {
            Settings.wizardComplete(this);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Handle Dropbox plugin auth
        if (pluginChosen) {
            Pw pw = Pw.getInstance();
            String token = pw.resume();
            if (token != null) {
                Settings.put(C.S_TOKEN, token);
                closeButton.setEnabled(true);
            } else {
                // FIXME здесь надо как-то обрабатывать ситуацию если нет токена
            }
        }
    }
}
