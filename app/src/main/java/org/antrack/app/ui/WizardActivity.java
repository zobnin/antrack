package org.antrack.app.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.antrack.app.C;
import org.antrack.app.Init;
import org.antrack.app.Pw;
import org.antrack.app.Settings;
import org.antrack.app.libs.Admin;
import org.antrack.app.libs.Shell;
import org.antrack.app.libs.Utils;

import app.R;

public class WizardActivity extends AppCompatActivity {
    boolean pluginChoise = false;
    Admin aTools;

    Button button_close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context context = this;
        setContentView(R.layout.activity_wizard);

        Init.all(this);

        final Activity activity = this;

        final Button button_admin = (Button) findViewById(R.id.button_admin);
        button_admin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                aTools = new Admin(activity);
                aTools.showDialog(activity);
            }
        });


        final Button button_root = (Button) findViewById(R.id.button_root);
        if (Shell.checkSu()) {
            button_root.setOnClickListener(new View.OnClickListener() {
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
            button_root.setEnabled(false);
        }

        final Button button_dropbox = (Button) findViewById(R.id.button_dropbox);
        button_dropbox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Settings.put(C.S_PLUGIN, "dropbox");
                try {
                    pluginChoise = true;
                    Pw pw = Pw.getInstance();
                    pw.auth(activity);
                } catch (InterruptedException e) {
                    Utils.showToast(WizardActivity.this, "No internet, try later");
                }
            }
        });

        button_close = (Button) findViewById(R.id.button_close);
        button_close.setEnabled(false);
        button_close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Settings.put(C.S_LAUNCH_WIZARD, C.FALSE);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (Settings.get(C.S_TOKEN) == null) {
            // FIXME translate
            Utils.showToast(WizardActivity.this, "Authentication is required");
        } else {
            Settings.put(C.S_LAUNCH_WIZARD, C.FALSE);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Handle Dropbox plugin auth
        if (pluginChoise) {
            Pw pw = Pw.getInstance();
            String token = pw.resume();
            if (token != null) {
                Settings.put(C.S_TOKEN, token);
                button_close.setEnabled(true);
            } else {
                // FIXME здесь надо как-то обрабатывать ситуацию если нет токена
            }
        }
    }
}
