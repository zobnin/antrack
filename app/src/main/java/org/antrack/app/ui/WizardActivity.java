package org.antrack.app.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.antrack.app.C;
import org.antrack.app.Init;
import org.antrack.app.Pw;
import org.antrack.app.Settings;
import org.antrack.app.libs.Admin;
import org.antrack.app.libs.Shell;
import org.antrack.app.libs.Utils;

import app.R;

public class WizardActivity extends ActionBarActivity {
    boolean pluginChoise = false;
    Admin aTools;

    Button button_close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        final TextView textView_root = (TextView) findViewById(R.id.textView_root);
        //if (Shell.checkSu()) {
            //textView_root.setText("Su exist!");
        //}

        final Context context = this;

        final Button button_root = (Button) findViewById(R.id.button_root);
        button_root.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (Shell.checkSuRun()) {
                    Settings.put(C.S_USE_ROOT, "true");
                    Utils.showToast(context, "Root rights granted");
                } else {
                    Utils.showToast(context, "No root rights");
                }
            }
        });

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
                Settings.put("launchWizard", "false");
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
            Settings.put("launchWizard", "false");
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
                Settings.put("token", token);
                button_close.setEnabled(true);
            } else {
                // FIXME здесь надо как-то обрабатывать ситуацию если нет токена
            }
        }
    }
}
