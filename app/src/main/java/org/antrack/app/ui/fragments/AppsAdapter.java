package org.antrack.app.ui.fragments;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.antrack.app.ui.RecyclerViewAnim;
import org.antrack.app.ui.U;

import java.util.List;

import app.R;

class AppsAdapter extends RecyclerViewAnim.Adapter<AppsAdapter.ModuleViewHolder> {
    static class ModuleViewHolder extends RecyclerView.ViewHolder {
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

    private Activity activity;
    private List<App> apps;

    AppsAdapter(Activity activity, List<App> apps){
        this.activity = activity;
        this.apps = apps;
    }

    public void update(List<App> apps) {
        this.apps = apps;
        notifyDataSetChanged();
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

        viewHolder.appStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (Mod.check(Mod.STARTAPP)) {
                    U.runCommandAsync(Mod.STARTAPP + " " + apps.get(i).pkg);
                } else {
                    Mod.showNoModule(activity, Mod.STARTAPP);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

}
