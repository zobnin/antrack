package org.antrack.app.ui.fragments;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.antrack.app.ui.RecyclerViewAnim;
import org.antrack.app.ui.U;

import java.util.List;

import app.R;

class ContactsAdapter extends RecyclerViewAnim.Adapter<ContactsAdapter.ModuleViewHolder> {
    private Activity activity;

    static class ModuleViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView name;
        TextView number;
        ImageButton call;
        ImageButton message;

        ModuleViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cardview_contacts);
            name = (TextView) itemView.findViewById(R.id.cardview_contacts_name);
            number = (TextView) itemView.findViewById(R.id.cardview_contacts_number);
            call = (ImageButton) itemView.findViewById(R.id.cardview_contacts_call);
            message = (ImageButton) itemView.findViewById(R.id.cardview_contacts_message);
        }
    }

    private List<Contact> contacts;

    ContactsAdapter(Activity activity, List<Contact> contacts){
        this.activity = activity;
        this.contacts = contacts;
    }

    public void update(List<Contact> contacts) {
        this.contacts = contacts;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ModuleViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_contacts, viewGroup, false);
        return new ModuleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ModuleViewHolder viewHolder, final int i) {
        viewHolder.name.setText(contacts.get(i).name);
        viewHolder.number.setText(contacts.get(i).number);

        // FIXME Выводить предупреждение о звонке
        viewHolder.call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Mod.check(Mod.DIAL)) {
                    String number = contacts.get(i).number;
                    number = number.replace(" ", "");
                    number = number.replace("-", "");
                    U.runCommandAsync("dial " + number);
                } else {
                    Mod.showNoModule(activity, Mod.DIAL);
                }
            }
        });

        viewHolder.message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Mod.check(Mod.SMS)) {
                    SendSmsDialog.show(activity, contacts.get(i).number, null);
                } else {
                    Mod.showNoModule(activity, Mod.SMS);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

}
