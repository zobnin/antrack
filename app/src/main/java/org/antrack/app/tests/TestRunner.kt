package org.antrack.app.tests

import android.app.Activity
import android.app.AlertDialog
import org.antrack.app.functions.className
import org.antrack.app.functions.highlightBooleans
import org.antrack.app.functions.logD
import org.antrack.app.functions.toast

/*
 * What to test
 * - Internal commands
 * - Module commands
 * - Multi commands
 * - Dumb cloud provider to test cloud send
 */

class TestRunner(private val activity: Activity) {

    fun run() {
        logD(className, "Testing started")
        activity.toast("Testing started")

        Thread {
            val results = ModulesTests(activity.applicationContext).run()
            activity.runOnUiThread {
                showResultDialog(
                    results.joinToString("\n")
                        .highlightBooleans()
                )
            }
        }.start()
    }

    private fun showResultDialog(result: CharSequence) {
        AlertDialog.Builder(activity)
            .setTitle("Test results")
            .setMessage(result)
            .setPositiveButton(android.R.string.ok) { _, _ -> }
            .show()
    }
}