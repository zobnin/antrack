package org.antrack.app;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

public class Features {
    private static final String TAG = "Features";

    private Context context;

    public boolean root = false;
    public boolean admin = false;
    public boolean backCamera = false;
    public boolean frontCamera = false;
    public boolean phone = false;

    public Features(Context context) {
        this.context = context;
        read();
    }

    private void read() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Init.MAIN_DIR + C.FEATURES_FILE));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] pair = line.split(":");
                switch (pair[0].trim()) {
                    case "root":
                        root = true;
                        break;
                    case "admin":
                        admin = true;
                        break;
                    case "backCamera":
                        backCamera = true;
                        break;
                    case "frontCamera":
                        frontCamera = true;
                        break;
                    case "phone":
                        phone = true;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Read exception: " + e.toString());
        }
    }

    public void write() {
        getFeatures();

        try {
            FileWriter writer = new FileWriter(Init.MAIN_DIR + C.FEATURES_FILE);

            String feat = "";
            if (root) feat += "root\n";
            if (admin) feat += "admin\n";
            if (backCamera) feat += "backCamera\n";
            if (frontCamera) feat += "frontCamera\n";
            if (phone) feat += "phone\n";

            writer.write(feat);
            writer.close();
        } catch (Exception e) {
            Log.e(TAG, "Write error: " + e.toString());
        }
    }

    private void getFeatures() {
        Settings.init();

        // Do we have root?
        if (Settings.get(C.S_USE_ROOT).equals("true")) {
            root = true;
        }

        // Do we have admin?
        if (Settings.get(C.S_USE_ADMIN).equals("true")) {
            admin = true;
        }

        // Do we have cameras?
        int cameras = Camera.getNumberOfCameras();
        if (cameras > 0) {
            backCamera = true;
        }
        if (cameras > 1) {
            frontCamera = true;
        }

        // Do we have phone?
        PackageManager pm = context.getPackageManager();
        phone = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }
}
