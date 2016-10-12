package org.antrack.app.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.antrack.app.C;
import org.antrack.app.ui.RecyclerViewAnim;
import org.antrack.app.ui.V;

import app.R;

public class ModulesFragment extends BaseFragment {
    final String TAG = "InfoFragment";
    Context context;

    RecyclerViewAnim recyclerView;
    ModulesAdapter modulesAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity().getApplicationContext();

        if (V.modules.isEmpty())
            showNodata();

        View view = inflater.inflate(R.layout.fragment_cardview, null);

        recyclerView = (RecyclerViewAnim) view.findViewById(R.id.fragment_cardview_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);

        modulesAdapter = new ModulesAdapter(V.modules);
        recyclerView.setAdapter(modulesAdapter);

        return view;
    }

    @Override
    public String getName() { return "Mod"; }
}
