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

public class CallsAdapter extends RecyclerViewAnim.Adapter<CallsAdapter.ModuleViewHolder> {
    public static class ModuleViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView callTime;
        TextView callDirection;
        TextView callNumber;

        ModuleViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cardview_calls);
            callTime = (TextView)itemView.findViewById(R.id.cardview_calls_time);
            callDirection = (TextView)itemView.findViewById(R.id.cardview_calls_direction);
            callNumber = (TextView)itemView.findViewById(R.id.cardview_calls_number);
        }
    }

    List<Call> calls;

    CallsAdapter(List<Call> calls){
        this.calls = calls;
    }

    public void update(List<Call> calls) {
        this.calls = calls;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ModuleViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_calls, viewGroup, false);
        return new ModuleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ModuleViewHolder viewHolder, int i) {
        viewHolder.callTime.setText(calls.get(i).date + " " + calls.get(i).time);
        viewHolder.callDirection.setText(calls.get(i).direction);
        viewHolder.callNumber.setText(calls.get(i).number);
    }

    @Override
    public int getItemCount() {
        return calls.size();
    }

}
