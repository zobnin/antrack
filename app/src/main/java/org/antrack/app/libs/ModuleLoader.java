package org.antrack.app.libs;

import android.content.Context;
import android.util.Log;

import org.antrack.app.C;
import org.antrack.app.ModuleInterface;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import dalvik.system.DexClassLoader;

public class ModuleLoader {
    private static String TAG="ModuleLoader";

    public Class<?> loadClass(File file, String odexDir) {
        DexClassLoader classLoader = new DexClassLoader(
                file.getPath(), odexDir, null, getClass().getClassLoader());
        try {
            return classLoader.loadClass(C.APP_NAME + ".modules." + file.getName().replace(".jar", "") + ".Module");
        } catch (Exception e) {
            Log.e(TAG, "Load class error: " + e.toString());
            return null;
        }
    }

    public Map<String, ModuleInterface> getObjects(String jarDir, String odexDir) {
        File[] files = new File(jarDir).listFiles();

        if (files == null) {
            Log.e(TAG, "getObjects: There was no files in " + jarDir);
            return null;
        }

        Map<String, ModuleInterface> hashmap = new HashMap<>();

        for (File file : files) {
            Class<?> loadedClass = loadClass(file, odexDir);
            if (loadedClass == null) continue;
            try {
                ModuleInterface obj = (ModuleInterface) loadedClass.newInstance();
                hashmap.put(file.getName().replace(".jar", ""), obj);
            } catch (Exception e) {
                Log.d(TAG, "getObjects error: " + e.toString());
            }
        }
        return hashmap;
    }

    private Method getMethod(Object obj, String name, Class<?>... parameterTypes) {
        try {
            return obj.getClass().getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean unpackModules(Context context, String modulePath) {
        try {
            String[] moduleList = context.getAssets().list("modules");

            if (moduleList.length == 0) {
                Log.d(TAG, "unpackModules: no modules");
                return false;
            }

            BufferedInputStream bis;
            OutputStream dexWriter;
            final int BUF_SIZE = 8 * 1024;

            for (String module : moduleList) {
                Log.d(TAG, "unpackModules: unpacking " + module);
                try {
                    bis = new BufferedInputStream(context.getAssets().open("modules/" + module));
                    dexWriter = new BufferedOutputStream(
                            new FileOutputStream(modulePath + "/" + module));
                    byte[] buf = new byte[BUF_SIZE];
                    int len;
                    while((len = bis.read(buf, 0, BUF_SIZE)) > 0) {
                        dexWriter.write(buf, 0, len);
                    }
                    dexWriter.close();
                    bis.close();

                } catch (Exception e) {
                    Log.e(TAG, "unpackModules error: " + e.toString());
                }
            }

        } catch (IOException e) {
            Log.e(TAG, "unpackModules error: " + e.toString());
        }
        return true;
    }
}
