package org.antrack.app.ui.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.R
import org.antrack.app.libs.L
import org.antrack.app.ui.RecyclerViewAnim
import org.antrack.app.ui.State
import org.antrack.app.ui.U
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AppsFragment : BaseFragment() {
    private val TAG = "AppsFragment"

    override val module = "apps"

    private lateinit var executor: ExecutorService
    private lateinit var appsAdapter: AppsAdapter
    private lateinit var recyclerView: RecyclerViewAnim

    private var apps = ArrayList<App>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Otherwise GetActivity() return null after orientation change
        retainInstance = true

        checkModule() || return null

        val view = inflater.inflate(R.layout.fragment_cardview, container, false)

        val context = activity.applicationContext
        recyclerView = view.findViewById(R.id.fragment_cardview_list) as RecyclerViewAnim
        val linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager

        val apps = ArrayList<App>()
        appsAdapter = AppsAdapter(activity, apps)
        recyclerView.adapter = appsAdapter

        executor = Executors.newFixedThreadPool(1)

        onFileUpdate()

        command?.let { U.runCommandAsync(it) }

        if (!State.device.isMain) {
            watchFile?.let {
                U.getFileAsync(it)
            }
        }

        return view
    }

    override fun onFileUpdate() {
        executor.submit(Runnable {
            waitCardsDrawn(recyclerView)

            apps = ArrayList<App>()

            if (!readFile() || apps.isEmpty()) {
                showNoDataOrLoading()
                return@Runnable
            }

            val activity = activity ?: return@Runnable

            activity.runOnUiThread {
                appsAdapter.update(apps)
                hideAllMessages()
            }
        })
    }

    private fun readFile(): Boolean {
        watchFile?.let {
            val path = U.getLocalPath(it)
            val file = File(path)

            if (!file.exists() || file.length() < 1) {
                return false
            }

            try {
                val reader = BufferedReader(FileReader(path))

                for (line in reader.readLines()) {
                    val pair = line.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                    if (pair.size < 2)
                        continue

                    val app = App()
                    app.name = pair[0]
                    app.pkg = pair[1].trim { it <= ' ' }

                    apps.add(app)
                }
            } catch (e: IOException) {
                L.e(TAG, "Can't read " + this.watchFile + ": " + e)
                return false
            }

        } ?: return false

        return true
    }
}
