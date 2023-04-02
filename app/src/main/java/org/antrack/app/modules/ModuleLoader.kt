package org.antrack.app.modules

import android.content.Context
import copyTo
import dalvik.system.DexClassLoader
import org.antrack.app.APP_NAME
import org.antrack.app.MODULES_ASSET_DIR
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

    private fun unpackModules(): Boolean {
        val modules = context.assets.list(MODULES_ASSET_DIR)
            ?: throw IllegalStateException("no modules")

        modules.forEach { module ->
            unpackModule(module)
        }

        return true
    }

    private fun unpackModule(module: String?) {
        try {
            logD(className, "Unpacking $module")
            val iStream = context.assets.open("$MODULES_ASSET_DIR/$module")
            val oStream = File("$modDir/$module").outputStream()
            iStream.copyTo(oStream)
        } catch (e: Exception) {
            logE(className, "Unpack module error: $e")
            e.printStackTrace()
        }
    }

    private fun getModuleFiles(): List<File> {
        val files = File(modDir).listFiles()
            ?: throw IllegalArgumentException("There are no module files")

        return files
            .toList()
            .filter { it.isFile }
    }

    private fun loadModules(
        files: List<File>,
    ): Map<String, ModuleInterface> {

        val modMap = mutableMapOf<String, ModuleInterface>()

        files.forEach { file ->
            val modName = file.name.removeJarExt()
            val module = loadModule(file) ?: return@forEach
            modMap[modName] = module
        }

        return modMap
    }

    private fun loadModule(file: File): ModuleInterface? {
        try {
            logD(className, "Loading module ${file.name}...")
            return loadClass(file).newInstance() as ModuleInterface
        } catch (e: Exception) {
            logE(className, "Can't load module ${file.name}: ${e.message}")
            e.printStackTrace()
        }

        return null
    }

    private fun loadClass(file: File): Class<*> {
        val classLoader = DexClassLoader(
            file.path, null, null, javaClass.classLoader
        )

        val moduleName = file.name.removeJarExt()
        val className = moduleName.toModuleClassName()

        return classLoader.loadClass(className)
    }

    private fun String.removeJarExt() = removeSuffix(".jar")
    private fun String.toModuleClassName() = "$APP_NAME.modules.$this.Module"
}
