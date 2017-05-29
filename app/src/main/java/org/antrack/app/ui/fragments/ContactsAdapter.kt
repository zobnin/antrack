package org.antrack.app.ui.fragments

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import app.R
import org.antrack.app.ui.U

internal class ContactsAdapter(
        private val activity: Activity,
        private var contacts: List<Contact>?) :
        RecyclerView.Adapter<ContactsAdapter.ModuleViewHolder>() {

    internal class ModuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.cardview_contacts_name) as TextView
        var number: TextView = itemView.findViewById(R.id.cardview_contacts_number) as TextView
        var call: ImageButton = itemView.findViewById(R.id.cardview_contacts_call) as ImageButton
        var message: ImageButton = itemView.findViewById(R.id.cardview_contacts_message) as ImageButton

    }

    fun update(contacts: List<Contact>) {
        this.contacts = contacts
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ModuleViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.cardview_contacts, viewGroup, false)
        return ModuleViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ModuleViewHolder, i: Int) {
        viewHolder.name.text = contacts!![i].name
        viewHolder.number.text = contacts!![i].number

        // FIXME Выводить предупреждение о звонке
        viewHolder.call.setOnClickListener {
            if (ModUtils.checkModule(Mod.DIAL)) {
                var number = contacts!![i].number
                number = number!!.replace(" ", "")
                number = number.replace("-", "")
                U.runCommandAsync(Mod.DIAL + " " + number)
            } else {
                ModUtils.showNoModuleToast(activity, Mod.DIAL)
            }
        }

        viewHolder.message.setOnClickListener {
            if (ModUtils.checkModule(Mod.SMS)) {
                SendSmsDialog.show(activity, contacts!![i].number, null)
            } else {
                ModUtils.showNoModuleToast(activity, Mod.SMS)
            }
        }
    }

    override fun getItemCount(): Int {
        return contacts!!.size
    }

}
