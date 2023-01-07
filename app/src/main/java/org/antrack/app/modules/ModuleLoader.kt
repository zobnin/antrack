package org.antrack.app.modules

import android.content.Context
import copyTo
import dalvik.system.DexClassLoader
import org.antrack.app.APP_NAME
import org.antrack.app.functions.className
import org.antrack.app.functions.logD
import org.antrack.app.functions.logE
import java.io.File

class ModuleLoader(
    private val context: Context,
    private val modDir: String,
) {
    fun getModuleObjects(): Map<String, ModuleInterface> {
        unpackModules()

        val files = getModuleFiles()
        val objects = loadModules(files)

        return objects
    }

    private fun getModuleFiles(): List<File> {
        val files = File(modDir).listFiles()
            ?: throw IllegalArgumentException("There is no module files")

        return files.toList().filter { it.isFile }
    }

    private fun loadModules(
        files: List<File>,
    ): Map<String, ModuleInterface> {

        val hashmap = mutableMapOf<String, ModuleInterface>()

        files.forEach { file ->
            val modName = file.name.removeSuffix(".jar")
            try {
                val modObj = loadClass(file).newInstance() as ModuleInterface
                hashmap[modName] = modObj
                logD(className, "Module loaded: $modName")
            } catch (e: Exception) {
                e.printStackTrace()
                logE(className, "Can't load module: $modName")
            }
        }

        return hashmap
    }

    private fun loadClass(file: File): Class<*> {
        val classLoader = DexClassLoader(
            file.path, null, null, javaClass.classLoader
        )

        val moduleName = file.name.removeSuffix(".jar")
        val className = "$APP_NAME.modules.$moduleName.Module"

        return classLoader.loadClass(className)
    }

    private fun unpackModules(): Boolean {
        val modules = context.assets.list("modules")
            ?: throw IllegalStateException("no modules")

        modules.forEach { module ->
            try {
                logD(className, "unpackModules: unpacking $module")
                val iStream = context.assets.open("modules/$module")
                val oStream = File("$modDir/$module").outputStream()
                iStream.copyTo(oStream)
            } catch (e: Exception) {
                logE(className, "unpackModules error: $e")
            }
        }

        return true
    }
}
