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

class CallsFragment : BaseFragment() {
    internal val TAG = "CallsFragment"

    override val module = Mod.LOGCALLS

    private lateinit var executor: ExecutorService
    private lateinit var callsAdapter: CallsAdapter
    private lateinit var recyclerView: RecyclerViewAnim

    private var calls: MutableList<Call>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Otherwise GetActivity() return null after orientation change
        retainInstance = true

        checkModule() || return null
        checkPhone() || return null

        val view = inflater.inflate(R.layout.fragment_cardview, container, false)

        recyclerView = view.findViewById(R.id.fragment_cardview_list) as RecyclerViewAnim
        val linearLayoutManager = LinearLayoutManager(activity.applicationContext)
        recyclerView.layoutManager = linearLayoutManager

        calls = ArrayList<Call>()
        callsAdapter = CallsAdapter(calls)
        recyclerView.adapter = callsAdapter

        executor = Executors.newFixedThreadPool(1)

        onFileUpdate()

        // Call getFile when view created, view will be updated when files downloaded
        if (!State.device!!.isMain) {
            U.getFileAsync(watchFile!!)
        }

        return view
    }

    override fun onFileUpdate() {
        executor.submit(Runnable {
            waitCardsDrawn(recyclerView)

            calls = ArrayList<Call>()

            if (!readFile() || calls!!.isEmpty()) {
                showNoDataOrLoading()
                return@Runnable
            }

            if (activity == null) return@Runnable
            activity.runOnUiThread {
                callsAdapter.update(calls as ArrayList)
                callsAdapter.notifyDataSetChanged()
                hideAllMessages()
            }
        })
    }

    private fun readFile(): Boolean {
        val path = U.getLocalPath(watchFile!!)

        if (!File(path).exists() || File(path).length() < 1) {
            return false
        }

        try {
            val reader = BufferedReader(FileReader(U.getLocalPath(watchFile!!)))
            for (line in reader.readLines()) {
                val pair = line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (pair.size < 4)
                    continue
                val call = Call()
                call.date = pair[0]
                call.time = pair[1]
                call.direction = pair[2]
                call.number = pair[3]
                calls!!.add(call)
            }
        } catch (e: IOException) {
            L.e(TAG, "Can't read apps file: " + e)
            return false
        }

        // We need calls from last
        Collections.reverse(calls!!)

        return true
    }
}
