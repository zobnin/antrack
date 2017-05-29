package org.antrack.app.ui.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.R
import org.antrack.app.libs.Files
import org.antrack.app.libs.L
import org.antrack.app.ui.RecyclerViewAnim
import org.antrack.app.ui.State
import org.antrack.app.ui.U
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class InfoFragment : BaseFragment() {
    private val TAG = "InfoFragment"

    override val module = Mod.INFO

    private val infoFile: String?
        get() = State.device.modules[Mod.INFO]?.result

    private val statusFile: String?
        get() = State.device.modules[Mod.STATUS]?.result

    lateinit private var executor: ExecutorService
    lateinit private var recyclerView: RecyclerViewAnim
    lateinit private var infoAdapter: InfoAdapter

    private var infos = ArrayList<Info>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Otherwise GetActivity() return null after orientation change
        retainInstance = true

        checkModule(Mod.INFO) || return null
        checkModule(Mod.STATUS) || return null

        val view = inflater.inflate(R.layout.fragment_cardview, container, false)

        recyclerView = view.findViewById(R.id.fragment_cardview_list) as RecyclerViewAnim
        val linearLayoutManager = LinearLayoutManager(activity.applicationContext)
        recyclerView.layoutManager = linearLayoutManager

        infos = ArrayList<Info>()
        infoAdapter = InfoAdapter(infos)
        recyclerView.adapter = infoAdapter

        executor = Executors.newFixedThreadPool(1)

        onFileUpdate()

        U.runCommandAsync("info; status")

        if (!State.device.isMain) {
            infoFile?.let { U.getFileAsync(it) }
            statusFile?.let { U.getFileAsync(it) }
        }

        return view
    }

    override fun onFileUpdate() {
        executor.submit(Runnable {
            waitCardsDrawn(recyclerView)

            infos = ArrayList<Info>()

            val info = readFile(infoFile!!, getString(R.string.device_info))
            if (info.data.isEmpty()) {
                showNoDataOrLoading()
                return@Runnable
            }

            val status = readFile(statusFile!!, getString(R.string.current_status))
            if (status.data.isEmpty()) {
                showNoDataOrLoading()
                return@Runnable
            }

            infos.add(info)
            infos.add(status)

            if (activity != null) {
                activity.runOnUiThread {
                    infoAdapter.updateInfos(infos)
                    infoAdapter.notifyDataSetChanged()
                    hideAllMessages()
                }
            }
        })
    }

    private fun readFile(file: String, title: String): Info {
        val path = U.getLocalPath(file)

        val info = Info()

        if (!File(path).exists()) {
            return info
        }

        try {
            val infoText = Files.readTextFile(path)
            info.title = title

            if (infoText != "")
                info.data = infoText.trim { it <= ' ' }
        } catch (e: IOException) {
            L.e(TAG, "Can't read file: " + e.toString())
        }

        return info
    }
}
