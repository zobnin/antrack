package org.antrack.app.ui.fragments;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.antrack.app.ui.Module;
import org.antrack.app.ui.RecyclerViewAnim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import app.R;

public class ModulesAdapter extends RecyclerViewAnim.Adapter<ModulesAdapter.ModuleViewHolder> {
    public static class ModuleViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView moduleName;
        TextView moduleDesc;
        TextView moduleVersion;
        TextView moduleAuthor;

        ModuleViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cardview_modules);
            moduleName = (TextView)itemView.findViewById(R.id.cardview_modules_name);
            moduleDesc = (TextView)itemView.findViewById(R.id.cardview_modules_desc);
            moduleVersion = (TextView)itemView.findViewById(R.id.cardview_modules_version);
            moduleAuthor = (TextView)itemView.findViewById(R.id.cardview_modules_author);
        }
    }

    LinkedHashMap<String, Module> modules;

    ModulesAdapter(LinkedHashMap<String, Module> modules){
        this.modules = modules;
    }

    public void update(LinkedHashMap<String, Module> modules) {
        this.modules = modules;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ModuleViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_modules, viewGroup, false);
        return new ModuleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ModuleViewHolder viewHolder, int i) {
        Module module = (Module)getElementByIndex(modules, i);
        viewHolder.moduleName.setText(module.name);
        viewHolder.moduleDesc.setText(module.desc);
        viewHolder.moduleVersion.setText("Version: " + module.version);
        viewHolder.moduleAuthor.setText("Author: " + module.author);
        // FIXME disabled: причина, кнопка launch
    }

    private Object getElementByIndex(LinkedHashMap map, int index){
        return map.get(map.keySet().toArray()[index]);
    }

    @Override
    public int getItemCount() {
        return modules.size();
    }

}
