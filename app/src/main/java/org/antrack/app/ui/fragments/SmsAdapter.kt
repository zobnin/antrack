package org.antrack.app.ui.fragments

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.R

internal class SmsAdapter(private var smses: List<Sms>?) : RecyclerView.Adapter<SmsAdapter.ModuleViewHolder>() {
    internal class ModuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cv: CardView
        var from: TextView
        var date: TextView
        var body: TextView
        var direction: TextView

        init {
            cv = itemView.findViewById(R.id.cardview_sms) as CardView
            from = itemView.findViewById(R.id.cardview_sms_from) as TextView
            date = itemView.findViewById(R.id.cardview_sms_date) as TextView
            body = itemView.findViewById(R.id.cardview_sms_body) as TextView
            direction = itemView.findViewById(R.id.cardview_sms_direction) as TextView
        }
    }

    fun update(smses: List<Sms>) {
        this.smses = smses
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ModuleViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.cardview_sms, viewGroup, false)
        return ModuleViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ModuleViewHolder, i: Int) {
        viewHolder.from.text = smses!![i].from
        viewHolder.date.text = smses!![i].date
        viewHolder.body.text = smses!![i].body

        val direction: Int
        when (smses!![i].direction) {
            "Outgoing" -> direction = R.string.outgoing
            "Answered" -> direction = R.string.answered
            "Incoming" -> direction = R.string.incoming
            else -> direction = R.string.incoming
        }
        viewHolder.direction.setText(direction)
    }

    override fun getItemCount(): Int {
        return smses!!.size
    }

}
