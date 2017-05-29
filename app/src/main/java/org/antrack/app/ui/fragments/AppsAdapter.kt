package org.antrack.app.ui.fragments

import android.app.Activity
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import app.R
import org.antrack.app.ui.U

internal class AppsAdapter(private val activity: Activity, private var apps: List<App>?) : RecyclerView.Adapter<AppsAdapter.ModuleViewHolder>() {
    internal class ModuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cv: CardView
        var appName: TextView
        var appPkg: TextView
        var appStart: Button

        init {
            cv = itemView.findViewById(R.id.cardview_apps) as CardView
            appName = itemView.findViewById(R.id.cardview_apps_name) as TextView
            appPkg = itemView.findViewById(R.id.cardview_apps_pkgname) as TextView
            appStart = itemView.findViewById(R.id.cardview_apps_start) as Button
        }
    }

    fun update(apps: List<App>) {
        this.apps = apps
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ModuleViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.cardview_apps, viewGroup, false)
        return ModuleViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ModuleViewHolder, i: Int) {
        viewHolder.appName.text = apps!![i].name
        viewHolder.appPkg.text = apps!![i].pkg

        viewHolder.appStart.setOnClickListener {
            // FIXME check module
            U.runCommandAsync(Mod.STARTAPP + " " + apps!![i].pkg)

        }
    }

    override fun getItemCount(): Int {
        return apps!!.size
    }

}
