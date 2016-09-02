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

public class InfoAdapter extends RecyclerViewAnim.Adapter<InfoAdapter.ModuleViewHolder> {
    List<Info> infos;

    public static class ModuleViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView title;
        TextView data;

        ModuleViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cardview_info);
            title = (TextView) itemView.findViewById(R.id.cardview_info_title);
            data = (TextView) itemView.findViewById(R.id.cardview_info_data);
        }
    }

    InfoAdapter(List<Info> infos){
        this.infos = infos;
    }

    public void updateInfos(List<Info> infos) {
        this.infos = infos;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ModuleViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_info, viewGroup, false);
        return new ModuleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ModuleViewHolder viewHolder, int i) {
        viewHolder.title.setText(infos.get(i).title);
        viewHolder.data.setText(infos.get(i).data);
    }

    @Override
    public int getItemCount() {
        return infos.size();
    }

}
