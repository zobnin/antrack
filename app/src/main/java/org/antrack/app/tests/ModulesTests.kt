package org.antrack.app.tests

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.pm.PackageManager
import android.media.AudioManager
import app.BuildConfig
import org.antrack.app.libs.Shell
import org.antrack.app.service.watcher.UploaderCallback
import org.antrack.app.ui.MainActivity
import org.antrack.app.watcher.FileWatcher
import java.io.File
import java.text.SimpleDateFormat


class ModulesTests(private val context: Context) : Test() {
    override fun run(): List<String> {
        // Slows down the tests
        FileWatcher.removeCallback("service_uploader")

        val screenshotResult = when {
            Shell.checkSu() -> testScreenshotModule()
            else -> "no root rights"
        }

        val notifyResult = when (PackageManager.PERMISSION_GRANTED) {
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) -> testNotifyModule()
            else -> "no notifications permission"
        }

        val audioResult = testAudioWithoutArg() && testAudioWithWrongArg() && testAudio()
        val cameraResult = testCameraWithoutArg() && testCameraWithWrongArg() && testCamera()
        val cmdResult = testCmdResultWithoutArg() && testCmd()
        val playResult = testPlayWithoutArg() && testPlayWithWrongArg() && testPlay()

        val results = listOf(
            // FIXME alarm влияет на результат теста play
            "alarm: " + testAlarm(),
            "apps: " + testApps(),
            "audio: " + audioResult,
            "camera: " + cameraResult,
            "cmd: " + cmdResult,
            "contacts: " + testContacts(),
            "dumpsms: " + testDumpSms(),
            "dial: " + testDialWithoutArg(),
            "hide: " + testHide(),
            "info: " + testInfo(),
            "status: " + testStatus(),
            "startapp: " + testStartAppWithoutArg(),
            "locate: " + testLocate(),
            "notify: " + notifyResult,
            "logcalls: " + "not testable",
            "play: " + playResult,
            "screenshot: " + screenshotResult,
            "sms: " + "not testable",
            "wipesd: " + "not testable",
        )

        FileWatcher.addCallback("service_uploader", UploaderCallback())

        return results
    }

    private fun testAlarm(): Boolean {
        return testModule("alarm") { out ->
            isMusicActive()
        }
    }

    private fun testApps(): Boolean {
        return testModule("apps") { out ->
            out.split("\n", limit = 2)
                .first()
                .split(": ").size == 2 &&
                    out.contains("AnTrack: org.antrack.app")
        }
    }

    private fun testAudioWithoutArg(): Boolean {
        return testModuleResult("audio", "audio", "error")
    }

    private fun testAudioWithWrongArg(): Boolean {
        return testModuleResult("audio", "audio 601", "error")
    }

    private fun testAudio(): Boolean {
        return testModule("audio", "audio 1") { out ->
            out.split("\n").first().endsWith(".3gp")
        }
    }

    private fun testCameraWithoutArg(): Boolean {
        return testModuleResult("camera", "camera", "error")
    }

    private fun testCameraWithWrongArg(): Boolean {
        return testModuleResult("camera", "camera foobar", "error")
    }

    private fun testCamera(): Boolean {
        return testModule("camera", "camera front") { out ->
            try {
                val isNameOk = out.split("\n").first().endsWith(".jpg")
                val lastFile = File(out.split("\n").last())
                isNameOk && lastFile.exists() && lastFile.length() > 0
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun testCmdResultWithoutArg(): Boolean {
        return testModuleResult("cmd", "cmd", "error")
    }

    @SuppressLint("SimpleDateFormat")
    private fun testCmd(): Boolean {
        return testModule("cmd", "cmd uname") { out ->
            val outLines = out.split("\n")

            val isDateOk = try {
                SimpleDateFormat("yyyy.MM.dd HH:mm:ss:SSS").parse(outLines[0])
                true
            } catch (e: Exception) {
                false
            }
            val isOutOk = outLines[1].trim() == "Linux"

            isDateOk && isOutOk
        }
    }

    private fun testContacts(): Boolean {
        return testModule("contacts") { out ->
            out.split("\n")
                .first()
                .split(": ")
                .size == 2
        }
    }

    private fun testDumpSms(): Boolean {
        return testModule("dumpsms") { out ->
            try {
                val fileNames = out.split("\n")
                val inboxFile = File(fileNames.find { it.endsWith("inbox") })
                val sentFile = File(fileNames.find { it.endsWith("sent") })
                val isInboxOk = inboxFile.readLines()
                    .first()
                    .startsWith("From:")
                val isSentOk = sentFile.readLines()
                    .first()
                    .startsWith("To:")
                isInboxOk && isSentOk
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun testDialWithoutArg(): Boolean {
        return testModuleResult("dial", "dial", "error")
    }

    private fun testHide(): Boolean {
        return testModule("hide", "hide on") {
            val pm = context.packageManager
            val cn = ComponentName(context, MainActivity::class.java)
            val isActivityDisabled = pm.getComponentEnabledSetting(cn) !=
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            pm.setComponentEnabledSetting(
                cn,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            isActivityDisabled
        }
    }

    private fun testInfo(): Boolean {
        return testModule("info") { out ->
            out.contains("Device name:")
        }
    }

    private fun testLocate(): Boolean {
        // FIXME проверять формат
        return testModule("locate") { out ->
            out.split("\n")
                .first()
                .split(" ").size == 4
        }
    }

    private fun testNotifyModule(): Boolean {
        return testModule("notify", "notify test") {
            val nm = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val notifications = nm.activeNotifications
            val isNotifyVisible = notifications.find { it.id == 0 } != null
            nm.cancelAll()
            isNotifyVisible
        }
    }

    private fun testPlayWithoutArg(): Boolean {
        return testModuleResult("play", "play", "error")
    }

    private fun testPlayWithWrongArg(): Boolean {
        return testModuleResult("play", "play /foo/bar", "error")
    }

    private fun testPlay(): Boolean {
        val samplePath = "/data/data/${BuildConfig.APPLICATION_ID}/alarm.ogg"
        return testModule("play", "play $samplePath") { out ->
            isMusicActive()
        }
    }

    private fun testScreenshotModule(): Boolean {
        return testModule("screenshot") { out ->
            try {
                val isNameOk = out.split("\n").first().endsWith(".png")
                val lastFile = File(out.split("\n").last())
                isNameOk && lastFile.exists() && lastFile.length() > 0
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun testStatus(): Boolean {
        return testModule("status") { out ->
            out.contains("Battery:")
        }
    }

    private fun testStartAppWithoutArg(): Boolean {
        return testModuleResult("startapp", "startapp", "error")
    }

    private fun isMusicActive(): Boolean {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val isMusicActive = am.isMusicActive
        // Set min value to not hear this noise :)
        am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
        return isMusicActive
    }
}