package org.antrack.app.tests

import android.app.Activity
import android.app.AlertDialog
import org.antrack.app.functions.className
import org.antrack.app.functions.highlightBooleans
import org.antrack.app.functions.logD
import org.antrack.app.functions.toast

class TestRunner(private val activity: Activity) {
    private val testStartedStr = "testing started"

    fun runModTests() {
        runTests("Modules", ModulesTests(activity.applicationContext))
    }

    fun runCmdTests() {
        runTests("Commands", CommandsTest(activity.applicationContext))
    }

    fun runCloudTests() {
        runTests("Cloud", CloudTest())
    }

    private fun runTests(name: String, test: Test) {
        logD(className, "$name $testStartedStr")
        activity.toast("$name $testStartedStr")

        Thread {
            test.before()
            val results = test.run()
            test.after()

            activity.runOnUiThread {
                showResultDialog(results)
            }
        }.start()
    }

    private fun showResultDialog(results: List<String>) {
        AlertDialog.Builder(activity)
            .setTitle("Test results")
            .setMessage(formatResults(results))
            .setPositiveButton(android.R.string.ok) { _, _ -> }
            .show()
    }

    private fun formatResults(results: List<String>): CharSequence {
        return results
            .joinToString("\n")
            .highlightBooleans()
    }
}