package org.antrack.app.ui.fragments

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.R
import org.antrack.app.ui.Module
import java.util.*

class ModulesAdapter internal constructor(internal var modules: LinkedHashMap<String, Module>) : RecyclerView.Adapter<ModulesAdapter.ModuleViewHolder>() {
    class ModuleViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var moduleName: TextView = itemView.findViewById(R.id.cardview_modules_name) as TextView
        internal var moduleDesc: TextView = itemView.findViewById(R.id.cardview_modules_desc) as TextView
        internal var moduleVersion: TextView = itemView.findViewById(R.id.cardview_modules_version) as TextView
        internal var moduleAuthor: TextView = itemView.findViewById(R.id.cardview_modules_author) as TextView
    }

    fun update(modules: LinkedHashMap<String, Module>) {
        this.modules = modules
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ModuleViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.cardview_modules, viewGroup, false)
        return ModuleViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ModuleViewHolder, i: Int) {
        val module = getElementByIndex(modules, i) as Module
        viewHolder.moduleName.text = module.name
        viewHolder.moduleDesc.text = module.desc
        viewHolder.moduleVersion.text = "Version: " + module.version!!
        viewHolder.moduleAuthor.text = "Author: " + module.author!!
    }

    private fun getElementByIndex(map: LinkedHashMap<*, *>, index: Int): Any? {
        return map[map.keys.toTypedArray()[index]]
    }

    override fun getItemCount(): Int {
        return modules.size
    }

}
