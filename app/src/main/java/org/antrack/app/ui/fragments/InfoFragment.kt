@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.antrack.app.ui.fragments

import android.text.Spannable
import app.R
import org.antrack.app.Env
import org.antrack.app.functions.*
import org.antrack.app.modules.ModulesSerializer
import org.antrack.app.ui.AppStatus
import org.antrack.app.watcher.FileWatcher
import org.antrack.app.watcher.IWatcherCallback
import java.io.File

class InfoFragment : ListBaseFragment() {
    private val modSerializer = ModulesSerializer()

    data class Info(
        val title: CharSequence,
        val data: CharSequence,
    )

    inner class FragmentCallback : IWatcherCallback {
        override val watchFile = "/status"
        override fun onFileUpdated(path: String) {
            readFilesAndUpdateAsync()
        }
    }

    override fun onStart() {
        super.onStart()
        readFilesAndUpdateAsync()
        FileWatcher.addCallback(className, FragmentCallback())
        runCommandAsync("!info; status")
    }

    override fun onStop() {
        super.onStop()
        FileWatcher.removeCallback(className)
    }

    private fun readFilesAndUpdateAsync() = async {
        try {
            val infos = readInfoAndStatus() + readAppInfo()
            val strings = infos.map(::infoToSpannable)
            showListInUiThread(strings)
            logD(className, "Fragment updated")
        } catch (e: Exception) {
            showException(e)
        }
    }

    private fun readInfoAndStatus(): List<Info> {
        // On first run there is no modules file so we don't throw the exception
        val modules = try {
            modSerializer.read()
        } catch (e: Exception) {
            return emptyList()
        }

        val infoModule = modules["info"] ?: throw IllegalStateException()
        val statusModule = modules["status"] ?: throw IllegalStateException()

        val info = fileToInfo(infoModule.result, getString(R.string.device_info))
        val status = fileToInfo(statusModule.result, getString(R.string.device_status))

        return listOf(info, status)
    }

    private fun readAppInfo(): Info {
        val status = AppStatus(context)
        return Info(
            title = getString(R.string.app_info),
            data = "Version: " + status.version + "\n" +
                    "Cloud plugin: " + status.cloudPlugin + "\n" +
                    "Service working: " + status.isServiceEnabled + "\n" +
                    "Can work in background: " + status.isIgnoringBatteryOptimizations + "\n" +
                    "Have access to all files: " + status.haveAccessToAllFiles + "\n" +
                    "Have admin rights: " + status.haveAdminRights + "\n" +
                    "Have root rights: " + status.haveRootRights
        )
    }

    private fun fileToInfo(file: String, title: String): Info {
        val path = Env.mainDirPath + file
        val infoText = File(path).readText()

        return Info(
            title = title,
            data = infoText.trim()
        )
    }

    private fun infoToSpannable(info: Info): Spannable {
        val title = info.title.bold()

        val data = when {
            info.data.isNotEmpty() -> info.data.highlightBooleans()
            else -> getString(R.string.loading)
        }

        return "\n".bold() +
                title + "\n\n" +
                data + "\n"
    }
}
