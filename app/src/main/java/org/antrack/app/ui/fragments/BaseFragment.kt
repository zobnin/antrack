package org.antrack.app.ui.fragments

import android.app.Fragment
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import app.R
import org.antrack.app.Pw
import org.antrack.app.libs.Files
import org.antrack.app.libs.L
import org.antrack.app.libs.Utils
import org.antrack.app.ui.*
import org.jetbrains.anko.find
import org.jetbrains.anko.runOnUiThread
import java.util.*

/* Фрагмент не должен ничего инициализировать заранее.
   Иначе можно столкнуться с тем, что объект уже создан, а данных еще нет.
 */

abstract class BaseFragment : Fragment() {
    private val waitThread: Thread? = null

    open val needSubtitle = true

    // Fragment must override them
    abstract fun onFileUpdate()
    abstract val module: String

    open val watchFile: String?
        get() = State.device.modules[module]?.result

    open val command: String?
        get() = State.device.modules[module]?.name

    fun checkModule(mod: String = module): Boolean {
        if (!State.device.modules.containsKey(mod)) {
            showNoModule()
            return false
        }
        return true
    }

    fun checkPhone(): Boolean {
        if (!State.device.features.phone) {
            showNoPhone()
            return false
        }
        return true
    }

    fun checkRoot(): Boolean {
        if (!State.device.features.root) {
            showNoRoot()
            return false
        }
        return true
    }

    fun showNoModuleToast() {
        activity.runOnUiThread {
            // FIXME translate
            Utils.showToast(activity, "Module not found: " + module)
        }
    }

    fun onResult(message: String) {
        val act = activity as MainActivity
        runOnUiThread { act.setToolbarTitle() }

        val messageA = message.split(" ".toRegex()).toTypedArray()
        if (messageA[0] == module) {
            if (messageA[1].contains("error")) {
                hideAllMessages()
                showError(Utils.arrayToString(Arrays.copyOfRange(messageA, 2, messageA.size)))
            } else {
                hideAllMessages()
            }
        }
    }

    protected fun waitCardsDrawn(rv: RecyclerViewAnim) {
        while (true) {
            if (!rv.mScrollable && !rv.mFirstUpdate) {
                try {
                    Thread.sleep(100)
                } catch (e: Exception) {
                    break
                }

            } else {
                break
            }
        }
    }

    private fun setVisible(v: View) {
        v.alpha = 0f
        v.visibility = View.VISIBLE
        v.animate().alpha(1f)
    }

    protected fun showError(text: String) {
        runOnUiThread {
            hideAllMessages()
            val errorView = activity.find<RelativeLayout>(R.id.error)
            val errorText = activity.find<TextView>(R.id.error_text)
            errorText.text = text
            setVisible(errorView)
        }
    }

    protected fun showNoDataOrLoading() {
        runOnUiThread {
            hideAllMessages()
            if (!State.device.isMain) {
                SnackBar.show(activity, getString(R.string.message_loading))
            } else {
                val noData = activity.find<RelativeLayout>(R.id.nodata)
                setVisible(noData)
            }
        }
    }

    protected fun showNoData() {
        runOnUiThread {
            hideAllMessages()
            setVisible(activity.find(R.id.nodata))
        }
    }

    protected fun hideNoData() {
        runOnUiThread {
            activity.find<RelativeLayout>(R.id.nodata).visibility = View.GONE
            activity.find<RelativeLayout>(R.id.loading).visibility = View.GONE
        }
    }

    // FIXME Module name
    protected fun showNoModule() {
        runOnUiThread {
            hideAllMessages()
            setVisible(activity.find(R.id.nomodule))
        }
    }

    protected fun hideNoModule() {
        runOnUiThread {
            activity.find<RelativeLayout>(R.id.nomodule).visibility = View.GONE
        }
    }

    protected fun showNoRoot() {
        runOnUiThread {
            hideAllMessages()
            setVisible(activity.find(R.id.noroot))
        }
    }

    protected fun hideNoRoot() {
        runOnUiThread {
            activity.find<RelativeLayout>(R.id.noroot).visibility = View.GONE
        }
    }

    protected fun showNoPhone() {
        runOnUiThread {
            hideAllMessages()
            setVisible(activity.find(R.id.nophone))
        }
    }

    protected fun hideNoPhone() {
        runOnUiThread {
            activity.find<RelativeLayout>(R.id.nophone).visibility = View.GONE
        }
    }

    fun hideAllMessages() {
        runOnUiThread {
            with (activity as MainActivity) {
                findViewById(R.id.error).visibility = View.GONE
                findViewById(R.id.nodata).visibility = View.GONE
                findViewById(R.id.loading).visibility = View.GONE
                findViewById(R.id.nomodule).visibility = View.GONE
                findViewById(R.id.noroot).visibility = View.GONE
                findViewById(R.id.nophone).visibility = View.GONE
                SnackBar.hide()
            }

            waitThread?.interrupt()
        }
    }

    fun deleteFiles() {
        Files.deleteDir(U.getLocalPath(watchFile!!), false)
        Thread(Runnable {
            try {
                Pw.delete(U.getCloudPath(watchFile!!.replace("/$".toRegex(), "")), false)
            } catch (e: Exception) {
                L.e("Mod", "Delete exception: " + e.toString())
            }
        }).start()
    }
}
