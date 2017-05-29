package org.antrack.app.ui.fragments

import android.app.Activity
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.R
import org.antrack.app.ui.U
import java.util.concurrent.TimeUnit

internal class AudioAdapter(
        private val activity: Activity,
        private var audios: List<Audio>?) :
        RecyclerView.Adapter<AudioAdapter.ModuleViewHolder>() {

    internal class ModuleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var cv: CardView = view.findViewById(R.id.cardview_audios) as CardView
        var length: TextView = view.findViewById(R.id.cardview_audios_length) as TextView
        var date: TextView = view.findViewById(R.id.cardview_audios_date) as TextView
    }

    fun update(audios: List<Audio>) {
        this.audios = audios
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ModuleViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.cardview_audios, viewGroup, false)
        return ModuleViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ModuleViewHolder, i: Int) {
        val a = audios!![i].file!!.substring(0, audios!![i].file!!.lastIndexOf('.')).split("-")
        val date = a[0] + "." + a[1] + "." + a[2] + " " + a[3] + ":" + a[4] + ":" + a[5]

        viewHolder.date.text = date
        viewHolder.length.text = String.format("%02d:%02d",
                TimeUnit.SECONDS.toMinutes(audios!![i].length),
                TimeUnit.SECONDS.toSeconds(audios!![i].length) % TimeUnit.MINUTES.toSeconds(1))

        viewHolder.cv.setOnClickListener {
            val aplayDialog = AudioPlayDialog(activity)
            aplayDialog.show(date, U.getLocalPath(ModUtils.getFile(Mod.AUDIO) + audios!![i].file))
        }
    }

    override fun getItemCount(): Int {
        return audios!!.size
    }

}
