package org.antrack.app.ui.fragments;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.antrack.app.ui.RecyclerViewAnim;
import org.antrack.app.ui.U;
import org.antrack.app.ui.V;

import java.util.List;

import app.R;

public class AppsAdapter extends RecyclerViewAnim.Adapter<AppsAdapter.ModuleViewHolder> {
    public static class ModuleViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView appName;
        TextView appPkg;
        Button   appStart;

        ModuleViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cardview_apps);
            appName = (TextView)itemView.findViewById(R.id.cardview_apps_name);
            appPkg = (TextView)itemView.findViewById(R.id.cardview_apps_pkgname);
            appStart = (Button)itemView.findViewById(R.id.cardview_apps_start);
        }
    }

    List<App> apps;

    AppsAdapter(List<App> apps){
        this.apps = apps;
    }

    public void update(List<App> apps) {
        this.apps = apps;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ModuleViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_apps, viewGroup, false);
        return new ModuleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ModuleViewHolder viewHolder, final int i) {
        viewHolder.appName.setText(apps.get(i).name);
        viewHolder.appPkg.setText(apps.get(i).pkg);

        // FIXME
        //if (V.modules.containsKey("startapp")) {
            viewHolder.appStart.setEnabled(true);
            viewHolder.appStart.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    U.runCommandAsync("startapp " + apps.get(i).pkg);
                }
            });
        //}
        // FIXME иконка
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

}
