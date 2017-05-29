package org.antrack.app.libs

import android.content.Context
import dalvik.system.DexClassLoader
import org.antrack.app.C
import org.antrack.app.ModuleInterface
import java.io.*
import java.lang.reflect.Method
import java.util.*

class ModuleLoader {

    private fun loadClass(file: File, odexDir: String): Class<*>? {
        val classLoader = DexClassLoader(
                file.path, odexDir, null, javaClass.classLoader)
        try {
            return classLoader.loadClass(C.APP_NAME + ".modules." + file.name.replace(".jar", "") + ".Module")
        } catch (e: Exception) {
            L.e(TAG, "Load class error: " + e.toString())
            return null
        }

    }

    fun getObjects(jarDir: String, odexDir: String): Map<String, ModuleInterface> {
        val files = File(jarDir).listFiles()

        if (files == null) {
            L.e(TAG, "getObjects: There was no files in " + jarDir)
        }

        val hashmap = HashMap<String, ModuleInterface>()

        for (file in files) {
            val loadedClass = loadClass(file, odexDir) ?: continue

            try {
                val obj = loadedClass.newInstance() as ModuleInterface
                hashmap.put(file.name.replace(".jar", ""), obj)
            } catch (e: Exception) {
                L.e(TAG, "getObjects error: " + e.toString())
            }

            L.d(TAG, "Module loaded: " + file.name.replace(".jar", ""))
        }
        return hashmap
    }

    private fun getMethod(obj: Any, name: String, vararg parameterTypes: Class<*>): Method? {
        try {
            return obj.javaClass.getMethod(name, *parameterTypes)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }

        return null
    }

    fun unpackModules(context: Context, modulePath: String): Boolean {
        try {
            val moduleList = context.assets.list("modules")

            if (moduleList.isEmpty()) {
                L.d(TAG, "unpackModules: no modules")
                return false
            }

            var bis: BufferedInputStream
            var dexWriter: OutputStream
            val BUF_SIZE = 8 * 1024

            for (module in moduleList) {
                try {
                    if (File(modulePath + "/" + module).exists()) {
                        L.d(TAG, "unpackModules: module $module exist")
                        continue
                    }

                    L.d(TAG, "unpackModules: unpacking $module")

                    bis = BufferedInputStream(context.assets.open("modules/" + module))
                    dexWriter = BufferedOutputStream(
                            FileOutputStream(modulePath + "/" + module))

                    val buf = ByteArray(BUF_SIZE)
                    var len: Int

                    while (true) {
                        len = bis.read(buf, 0, BUF_SIZE)
                        if (len < 0) { break }
                        dexWriter.write(buf, 0, len)
                    }

                    dexWriter.close()
                    bis.close()

                } catch (e: Exception) {
                    L.e(TAG, "unpackModules error: " + e.toString())
                }
            }

        } catch (e: IOException) {
            L.e(TAG, "unpackModules error: " + e.toString())
        }

        return true
    }

    companion object {
        private val TAG = "ModuleLoader"
    }
}
