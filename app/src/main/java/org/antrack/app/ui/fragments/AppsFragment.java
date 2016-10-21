package org.antrack.app.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.antrack.app.ui.RecyclerViewAnim;
import org.antrack.app.ui.U;
import org.antrack.app.ui.V;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import app.R;

public class AppsFragment extends BaseFragment {
    private final String TAG = "AppsFragment";

    private List<App> apps;
    private AppsAdapter appsAdapter;

    private String modFile;
    private String modCmd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Otherwise GetActivity() return null after orientation change
        setRetainInstance(true);

        if (!Mod.check(Mod.APPS)) {
            showNoModule(Mod.APPS);
            return null;
        }

        modFile = Mod.getFile(Mod.APPS);
        modCmd  = Mod.getCommand(Mod.APPS);

        View view = inflater.inflate(R.layout.fragment_cardview, null);

        Context context = getActivity().getApplicationContext();
        RecyclerViewAnim recyclerView = (RecyclerViewAnim) view.findViewById(R.id.fragment_cardview_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);

        apps = new ArrayList<>();
        appsAdapter = new AppsAdapter(getActivity(), apps);
        recyclerView.setAdapter(appsAdapter);

        onFileUpdate();

        U.runCommandAsync(modCmd);
        if (!V.currentDevice.isMain()) {
            U.getFileAsync(modFile);
        }

        return view;
    }

    @Override
    public String getName() { return "Applications"; }

    @Override
    public String getWatchFile() {
        return modFile;
    }

    @Override
    public void onFileUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apps = new ArrayList<>();

                if (!readFile() || apps.isEmpty()) {
                    showNoData();
                    return;
                }

                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        appsAdapter.update(apps);
                        appsAdapter.notifyDataSetChanged();
                        hideNoData();
                    }
                });
            }
        }).start();
    }

    private boolean readFile() {
        String path = U.getLocalPath(modFile);

        File file = new File(path);
        if (!file.exists() || file.length() == 0) {
            return false;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] pair = line.split(":");
                if (pair.length < 2)
                    continue;
                App app = new App();
                app.name = pair[0];
                app.pkg  = pair[1].trim();
                apps.add(app);
            }
        } catch (IOException e) {
            Log.e(TAG, "Can't read " + modFile + ": " + e);
            return false;
        }
        return true;
    }
}
