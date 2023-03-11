package org.antrack.app.tests

import android.app.Activity
import android.app.AlertDialog
import org.antrack.app.functions.className
import org.antrack.app.functions.highlightBooleans
import org.antrack.app.functions.logD
import org.antrack.app.functions.toast

class TestRunner(private val activity: Activity) {

    fun run() {
        logD(className, "Testing started")
        activity.toast("Testing started")

        Thread {
            val modTestResults = runModulesTests()

            activity.runOnUiThread {
                showResultDialog(modTestResults)
            }
        }.start()
    }

    private fun runModulesTests(): List<String> {
        val modTests = ModulesTests(activity.applicationContext)

        modTests.before()
        val results = modTests.run()
        modTests.after()

        return results
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