package org.antrack.app.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.antrack.app.Init;
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
    final private String TAG = "AppsFragment";

    private List<App> apps;

    private RecyclerViewAnim recyclerView;
    private AppsAdapter appsAdapter;

    private String MOD = "apps";

    private String modFile;
    private String modCmd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Otherwise GetActivity() return null after orientation change
        setRetainInstance(true);

        if(!V.modules.containsKey(MOD)) {
            // FIXME создавать view с сообщением о ненайденном модуле
            return null;
        }

        modFile = V.modules.get(MOD).result;
        modCmd  = V.modules.get(MOD).command;

        View view = inflater.inflate(R.layout.fragment_cardview, null);

        Context context = getActivity().getApplicationContext();
        recyclerView = (RecyclerViewAnim) view.findViewById(R.id.fragment_cardview_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);

        apps = new ArrayList<>();
        appsAdapter = new AppsAdapter(apps);
        recyclerView.setAdapter(appsAdapter);

        onFileUpdate();

        U.runCommandAsync(modCmd);
        if (!U.isDeviceMain()) {
            U.getFileAsync(modFile);
        }

        return view;
    }

    @Override
    public String getName() { return "Apps"; }

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
                readFile();

                if (getActivity() == null) return;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        appsAdapter.update(apps);
                        appsAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    private void readFile() {
        String path = U.getFullPath(modFile);

        if (!new File(path).exists()) {
            return;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] pair = line.split(":");
                if (pair.length < 2)
                    // FIXME надо выводить картинку, что данных нет
                    return;
                App app = new App();
                app.name = pair[0];
                app.pkg  = pair[1].trim();
                apps.add(app);
                // FIXME добавить сохранение иконки в кеш
            }
        } catch (IOException e) {
            Log.e(TAG, "Can't read " + modFile + ": " + e);
        }
    }
}
