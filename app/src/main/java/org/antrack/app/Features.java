package org.antrack.app;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;

import org.antrack.app.libs.L;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

public class Features {
    private static final String TAG = "Features";

    public boolean root = false;
    public boolean admin = false;
    public boolean backCamera = false;
    public boolean frontCamera = false;
    public boolean phone = false;

    public void read(String path) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
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
            L.d(TAG, "Read exception: " + e.toString());
        }
    }

    public void write(Context context, String path) {
        getFeatures(context);

        try {
            FileWriter writer = new FileWriter(path);

            String feat = "";
            if (root) feat += "root\n";
            if (admin) feat += "admin\n";
            if (backCamera) feat += "backCamera\n";
            if (frontCamera) feat += "frontCamera\n";
            if (phone) feat += "phone\n";

            writer.write(feat);
            writer.close();
        } catch (Exception e) {
            L.e(TAG, "Write error: " + e.toString());
        }
    }

    private void getFeatures(Context context) {
        // Do we have root?
        String haveRoot = Settings.getInstance().get(C.S_USE_ROOT);
        if (haveRoot != null && haveRoot.equals(C.TRUE)) {
            root = true;
        }

        // Do we have admin?
        String haveAdmin = Settings.getInstance().get(C.S_USE_ADMIN);
        if (haveAdmin != null && haveAdmin.equals(C.TRUE)) {
            root = true;
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
