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

class ContactsFragment : BaseFragment() {
    private val TAG = "ContactsFragment"

    override val module = "contacts"

    private var executor: ExecutorService? = null
    private var contactsAdapter: ContactsAdapter? = null
    private var recyclerView: RecyclerViewAnim? = null

    private var contacts: MutableList<Contact>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Otherwise GetActivity() return null after orientation change
        retainInstance = true

        checkModule() || return null

        val view = inflater.inflate(R.layout.fragment_cardview, container, false)

        val context = activity.applicationContext
        recyclerView = view.findViewById(R.id.fragment_cardview_list) as RecyclerViewAnim
        val linearLayoutManager = LinearLayoutManager(context)
        recyclerView!!.layoutManager = linearLayoutManager

        contacts = ArrayList<Contact>()
        contactsAdapter = ContactsAdapter(activity, contacts)
        recyclerView!!.adapter = contactsAdapter

        executor = Executors.newFixedThreadPool(1)

        onFileUpdate()

        U.runCommandAsync(command as String)
        if (!State.device!!.isMain) {
            U.getFileAsync(watchFile!!)
        }

        return view
    }

    override fun onFileUpdate() {
        executor!!.submit(Runnable {
            waitCardsDrawn(recyclerView!!)

            contacts = ArrayList<Contact>()
            readFile()

            if (contacts!!.isEmpty()) {
                showNoDataOrLoading()
                return@Runnable
            }

            if (activity == null) return@Runnable
            activity.runOnUiThread {
                contactsAdapter!!.update(contacts!!)
                contactsAdapter!!.notifyDataSetChanged()
                hideAllMessages()
            }
        })
    }

    private fun readFile() {
        val path = U.getLocalPath(watchFile!!)

        if (!File(path).exists()) {
            return
        }

        try {
            val reader = BufferedReader(FileReader(U.getLocalPath(watchFile!!)))
            var line: String
            for (line in reader.readLines()) {
                val pair = line.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (pair.size < 2)
                    continue
                val contact = Contact()
                contact.name = pair[0]
                contact.number = pair[1].trim { it <= ' ' }
                contacts!!.add(contact)
            }
        } catch (e: IOException) {
            L.e(TAG, "Can't read apps file: " + e)
        }

    }
}
