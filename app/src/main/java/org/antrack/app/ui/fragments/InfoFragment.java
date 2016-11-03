package org.antrack.app.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.antrack.app.Trial;
import org.antrack.app.libs.Files;
import org.antrack.app.ui.RecyclerViewAnim;
import org.antrack.app.ui.State;
import org.antrack.app.ui.U;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import app.R;

public class InfoFragment extends BaseFragment {
    final String TAG = "InfoFragment";
    Context context;

    RecyclerViewAnim recyclerView;
    InfoAdapter infoAdapter;

    List<Info> infos;

    String infoFile;
    String statusFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        infoFile = Mod.getFile(Mod.INFO);
        statusFile = Mod.getFile(Mod.STATUS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Otherwise GetActivity() return null after orientation change
        setRetainInstance(true);

        if (!Mod.check(Mod.INFO) && !Mod.check(Mod.STATUS)) {
            showNoModule(Mod.INFO + ", " + Mod.STATUS);
            return null;
        }

        context = getActivity().getApplicationContext();

        View view = inflater.inflate(R.layout.fragment_cardview, container, false);

        recyclerView = (RecyclerViewAnim) view.findViewById(R.id.fragment_cardview_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);

        infos = new ArrayList<>();
        infoAdapter = new InfoAdapter(infos);
        recyclerView.setAdapter(infoAdapter);

        onFileUpdate();

        U.runCommandAsync("info; status");
        if (!State.device.isMain()) {
            U.getFileAsync(infoFile);
            U.getFileAsync(statusFile);
        }

        return view;
    }

    @Override
    public void onFileUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                infos = new ArrayList<>();

                Info info = readFile(infoFile, getString(R.string.device_info));
                if (info == null) {
                    showNoData();
                    return;
                }

                Info status = readFile(statusFile, getString(R.string.current_status));
                if (status == null) {
                    showNoData();
                    return;
                }

                Info trial = new Info();
                trial.title = getString(R.string.trial_status);
                trial.data = getString(R.string.days_remaining) + " " + Trial.getRemainingDays();

                infos.add(info);
                infos.add(status);
                infos.add(trial);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            infoAdapter.updateInfos(infos);
                            infoAdapter.notifyDataSetChanged();
                            hideNoData();
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public String getWatchFile() {
        return statusFile;
    }

    private Info readFile(String file, String title) {
        String path = U.getLocalPath(file);

        if (!new File(path).exists()) {
            return null;
        }

        Info info = new Info();

        try {
            String infoText = Files.readTextFile(path);
            info.title = title;

            if (!infoText.equals(""))
                info.data = infoText.trim();
            else
                return null;
        } catch (IOException e) {
            Log.e(TAG, "Can't read file: " + e.toString());
        }

        return info;
    }
}
