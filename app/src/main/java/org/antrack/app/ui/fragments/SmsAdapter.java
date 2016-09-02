package org.antrack.app.ui.fragments;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.antrack.app.ui.RecyclerViewAnim;

import java.util.List;

import app.R;

public class SmsAdapter extends RecyclerViewAnim.Adapter<SmsAdapter.ModuleViewHolder> {
    public static class ModuleViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView from;
        TextView date;
        TextView body;

        ModuleViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cardview_sms);
            from = (TextView)itemView.findViewById(R.id.cardview_sms_from);
            date = (TextView)itemView.findViewById(R.id.cardview_sms_date);
            body = (TextView)itemView.findViewById(R.id.cardview_sms_body);
        }
    }

    List<Sms> smses;

    SmsAdapter(List<Sms> smses){
        this.smses = smses;
    }

    public void update(List<Sms> smses) {
        this.smses = smses;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ModuleViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_sms, viewGroup, false);
        return new ModuleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ModuleViewHolder viewHolder, int i) {
        viewHolder.from.setText(smses.get(i).from);
        viewHolder.date.setText(smses.get(i).date);
        viewHolder.body.setText(smses.get(i).body);
    }

    @Override
    public int getItemCount() {
        return smses.size();
    }

}
