@file:Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")

package org.antrack.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import app.R

open class ListBaseFragment : BaseFragment() {

    private val adapter by lazy {
        ArrayAdapter(
            context,
            android.R.layout.simple_list_item_1,
            mutableListOf<CharSequence>()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Otherwise GetActivity() return null after orientation change
        retainInstance = true

        val view = inflater.inflate(R.layout.fragment_list, container, false)
        val listView = view.findViewById<ListView>(R.id.list)

        listView.adapter = adapter

        return view
    }

    protected fun showLoadingIfAdapterEmpty() {
        if (adapter.isEmpty) {
            showLoading()
        }
    }

    protected fun showListInUiThread(strings: List<CharSequence>) {
        runOnUiThread {
            adapter.apply {
                clear()
                addAll(strings)
                notifyDataSetChanged()
            }
            hideMessage()
        }
    }
}