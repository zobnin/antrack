package org.antrack.app.service;

import android.content.Context;
import android.util.Log;

import org.antrack.app.C;
import org.antrack.app.Init;
import org.antrack.app.ModuleInterface;
import org.antrack.app.Settings;
import org.antrack.app.libs.ModuleLoader;
import org.antrack.app.libs.Shell;
import org.antrack.app.libs.Utils;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

class Modules {
    private String TAG = "Mod";
    private Context context;
    private Map<String, ModuleInterface> modules = null;

    Modules(Context context) {
        this.context = context;

        String modulesDir = Init.APP_DIR + C.MODULES_DIR;
        String odexDir = Init.APP_DIR + C.ODEX_DIR;

        Shell.runCommand("mkdir " + modulesDir);
        Shell.runCommand("mkdir " + odexDir);

        ModuleLoader ml = new ModuleLoader();
        ml.unpackModules(context, modulesDir);
        modules = ml.getObjects(modulesDir, odexDir);
    }

    public Map<String, ModuleInterface> get() {
        return modules;
    }

    private boolean checkForRoot() {
        String useRoot = Settings.get(C.S_USE_ROOT);
        return !(useRoot == null || useRoot.equals("false"));
    }

    private boolean checkForAdmin() {
        String useAdmin = Settings.get(C.S_USE_ADMIN);
        return !(useAdmin == null || useAdmin.equals("false"));
    }

    String command(String moduleName, String[] args) {
        ModuleInterface module = modules.get(moduleName);
        if (module != null) {
            if (module.usesRoot() && !checkForRoot())
                return "error: no root rights";
            if (module.usesAdmin() && !checkForAdmin())
                return "error: no admin rights";

            return module.onCommand(context, args);
        }
        return "error: no such module";
    }

    public void run(String action, String extra) {
        Log.d(TAG, "Get action: " + action);
        boolean root = checkForRoot();
        boolean admin = checkForAdmin();

        for (Map.Entry<String, ModuleInterface> mod : modules.entrySet()) {
            ModuleInterface module = mod.getValue();

            if (module.usesRoot() && !root) continue;
            if (module.usesAdmin() && !admin) continue;

            switch (action) {
                case "boot":
                    module.onBoot(context);
                    break;
                case "load":
                    if (module.result() != null && module.result().endsWith("/"))
                        Shell.runCommand("mkdir " + Init.MAIN_DIR + module.result());
                    module.onLoad(context);
                    break;
                case "alarm":
                    module.onAlarm(context);
                    break;
                case "screenOn":
                    module.onScreenOn(context);
                    break;
                case "incomingCall":
                    module.onIncomingCall(context, extra);
                    break;
                case "outgoingCall":
                    module.onOutgoingCall(context, extra);
            }
        }
    }

    String listModules() {
        if (modules == null)
            return "error: no modules";

        try {
            FileWriter writer = new FileWriter(Init.MAIN_DIR + C.MODULES_FILE);

            for (Map.Entry<String, ModuleInterface> mod : modules.entrySet()) {
                ModuleInterface module = modules.get(mod.getKey());

                String info = "";
                info += "Name: " + mod.getKey() + "\n";
                info += "Version: " + module.version() + "\n";
                info += "Author: " + module.author() + "\n";
                info += "Description: " + module.desc() + "\n";
                info += "Command: " + module.command() + "\n";
                info += "Uses root: " + module.usesRoot() + "\n";
                info += "Uses admin: " + module.usesAdmin() + "\n";
                info += "Result file: ";

                if (module.result().equals("")) {
                    info += "none\n";
                } else {
                    info += module.result() + "\n";
                }

                info += "Start when: " + Utils.arrayToString(module.startWhen()) + "\n\n";
                writer.write(info);
            }
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, "filed to write modules file: " + e.toString());
        }
        return "done";
    }

    String dumpJSON() {
        try {
            FileWriter writer = new FileWriter(Init.MAIN_DIR + C.MODULES_JSON_FILE);
            for (Map.Entry<String, ModuleInterface> mod : modules.entrySet()) {
                ModuleInterface module = mod.getValue();
                JSONObject obj = new JSONObject();
                obj.put("name", mod.getKey());
                obj.put("version", module.version());
                obj.put("author", module.author());
                obj.put("desc", module.desc());
                obj.put("startWhen", Utils.arrayToString(module.startWhen()));
                obj.put("command", module.command());
                obj.put("result", module.result());
                obj.put("resultType", module.resultType());
                obj.put("usesAdmin", module.usesAdmin());
                obj.put("usesRoot", module.usesRoot());
                writer.write(obj.toJSONString());
                writer.flush();
            }
            writer.close();
            return "done";
        } catch (IOException e) {
            Log.e(TAG, "filed to write modules.json file");
            return "error: " + e.toString();
        }
    }
}
