package org.antrack.app.ui.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.R
import org.antrack.app.C
import org.antrack.app.libs.L
import org.antrack.app.ui.RecyclerViewAnim
import org.antrack.app.ui.State
import org.antrack.app.ui.U
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class SmsFragment : BaseFragment() {
    private val TAG = "SmsFragment"

    override val module = Mod.DUMPSMS

    lateinit private var executor: ExecutorService
    lateinit private var smsAdapter: SmsAdapter
    lateinit private var recyclerView: RecyclerViewAnim

    private var smses = ArrayList<Sms>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Otherwise GetActivity() return null after orientation change
        retainInstance = true

        checkModule() || return null
        checkPhone() || return null

        val context = activity.applicationContext

        val view = inflater.inflate(R.layout.fragment_cardview, container, false)

        recyclerView = view.findViewById(R.id.fragment_cardview_list) as RecyclerViewAnim
        val linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager

        smses = ArrayList<Sms>()
        smsAdapter = SmsAdapter(smses)
        recyclerView.adapter = smsAdapter

        executor = Executors.newFixedThreadPool(1)

        onFileUpdate()

        command?.let {
            U.runCommandAsync(it)
        }

        if (!State.device.isMain) {
            watchFile?.let {
                U.getFileAsync(it + "inbox")
                U.getFileAsync(it + "sent")
            }
        }

        return view
    }

    override fun onFileUpdate() {
        executor.submit(Runnable {
            waitCardsDrawn(recyclerView)

            smses = ArrayList<Sms>()

            if (!readFile() || smses.isEmpty()) {
                showNoDataOrLoading()
                return@Runnable
            }

            if (activity == null) return@Runnable
            activity.runOnUiThread {
                smsAdapter.update(smses)
                smsAdapter.notifyDataSetChanged()
                hideAllMessages()
            }
        })
    }

    inner class SmsComparator : Comparator<Sms> {
        override fun compare(o1: Sms, o2: Sms): Int {
            val date1: Date
            val date2: Date

            val format = SimpleDateFormat(C.DEFAULT_TIME_FORMAT, Locale.US)
            try {
                date1 = format.parse(o1.date)
                date2 = format.parse(o2.date)
            } catch (e: Exception) {
                e.printStackTrace()
                return -1
            }

            return date1.compareTo(date2)
        }
    }

    private fun readFile(): Boolean {
        // FIXME
        val pathIn = U.getLocalPath(watchFile!! + "inbox")
        val pathOut = U.getLocalPath(watchFile!! + "sent")

        if (!File(pathIn).exists() && !File(pathOut).exists()) {
            return false
        }

        ReadBox(pathIn, true)
        ReadBox(pathOut, false)

        if (smses.isEmpty()) {
            return false
        }

        // FIXME DateComparator exception: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.lang.String.length()' on a null object reference
        Collections.sort(smses, SmsComparator())
        Collections.reverse(smses)

        return true
    }

    private fun ReadBox(path: String, inbox: Boolean): Boolean {
        try {
            val reader = BufferedReader(FileReader(path))
            var sms = Sms()

            reader.readLines().forEach { line ->
                val pair = line.split(":".toRegex())
                when (pair[0]) {
                    "From", "To" -> sms.from = line.replace((pair[0] + ":").toRegex(), "").trim { it <= ' ' }
                    "Date" -> sms.date = line.replace((pair[0] + ":").toRegex(), "").trim { it <= ' ' }
                    "Body" -> sms.body = line.replace((pair[0] + ":").toRegex(), "").trim { it <= ' ' }
                    else -> {
                        if (inbox) {
                            sms.direction = "Incoming"
                        } else {
                            sms.direction = "Outgoing"
                        }
                        if (sms.from != null && sms.date != null && sms.body != null) {
                            smses.add(sms)
                        }
                        sms = Sms()
                    }
                }
            }
        } catch (e: IOException) {
            L.e(TAG, "Can't read sms file: " + e)
            return false
        }

        return true
    }
}
