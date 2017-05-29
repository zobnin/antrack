package org.antrack.app.ui.fragments

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.R

class InfoAdapter internal constructor(internal var infos: List<Info>) :
        RecyclerView.Adapter<InfoAdapter.ModuleViewHolder>() {

    class ModuleViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var title: TextView = itemView.findViewById(R.id.cardview_info_title) as TextView
        internal var data: TextView = itemView.findViewById(R.id.cardview_info_data) as TextView
    }

    fun updateInfos(infos: List<Info>) {
        this.infos = infos
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ModuleViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.cardview_info, viewGroup, false)
        return ModuleViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ModuleViewHolder, i: Int) {
        viewHolder.title.text = infos[i].title
        viewHolder.data.text = infos[i].data
    }

    override fun getItemCount(): Int {
        return infos.size
    }

}
