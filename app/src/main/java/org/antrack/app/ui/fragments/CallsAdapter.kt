package org.antrack.app.ui.fragments

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.R

internal class CallsAdapter(private var calls: List<Call>?) : RecyclerView.Adapter<CallsAdapter.ModuleViewHolder>() {
    internal class ModuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var callTime: TextView = itemView.findViewById(R.id.cardview_calls_time) as TextView
        var callDirection: TextView = itemView.findViewById(R.id.cardview_calls_direction) as TextView
        var callNumber: TextView = itemView.findViewById(R.id.cardview_calls_number) as TextView
    }

    fun update(calls: List<Call>) {
        this.calls = calls
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ModuleViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.cardview_calls, viewGroup, false)
        return ModuleViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ModuleViewHolder, i: Int) {
        viewHolder.callTime.text = calls!![i].date + " " + calls!![i].time
        viewHolder.callNumber.text = calls!![i].number

        val direction: Int
        when (calls!![i].direction) {
            "Outgoing" -> direction = R.string.outgoing
            "Answered" -> direction = R.string.answered
            "Incoming" -> direction = R.string.incoming
            else -> direction = R.string.incoming
        }
        viewHolder.callDirection.setText(direction)
    }

    override fun getItemCount(): Int {
        return calls!!.size
    }

}
