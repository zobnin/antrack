package org.antrack.app.ui.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.R
import org.antrack.app.ui.RecyclerViewAnim
import org.antrack.app.ui.State

class ModulesFragment : BaseFragment() {
    internal val TAG = "ModulesFragment"

    override val module = ""
    override fun onFileUpdate() {}

    lateinit var recyclerView: RecyclerViewAnim
    lateinit var modulesAdapter: ModulesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (State.device.modules.isEmpty())
            showNoDataOrLoading()

        val view = inflater.inflate(R.layout.fragment_cardview, null)

        recyclerView = view.findViewById(R.id.fragment_cardview_list) as RecyclerViewAnim
        val linearLayoutManager = LinearLayoutManager(activity.applicationContext)
        recyclerView.layoutManager = linearLayoutManager

        modulesAdapter = ModulesAdapter(State.device.modules)
        recyclerView.adapter = modulesAdapter

        return view
    }
}
