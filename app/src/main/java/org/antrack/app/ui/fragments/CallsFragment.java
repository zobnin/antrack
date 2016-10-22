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
import java.util.Collections;
import java.util.List;

import app.R;

public class CallsFragment extends BaseFragment {
    final String TAG = "CallsFragment";

    Context context;

    private List<Call> calls;
    CallsAdapter callsAdapter;

    String modFile;
    String modCmd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Otherwise GetActivity() return null after orientation change
        setRetainInstance(true);

        if (!Mod.check(Mod.LOGCALLS)) {
            showNoModule(Mod.LOGCALLS);
            return null;
        }

        if (!V.features.phone) {
            showNoPhone();
            return null;
        }

        modFile = Mod.getFile(Mod.LOGCALLS);
        modCmd  = Mod.getCommand(Mod.LOGCALLS);

        context = getActivity().getApplicationContext();

        View view = inflater.inflate(R.layout.fragment_cardview, null);

        RecyclerViewAnim recyclerView = (RecyclerViewAnim) view.findViewById(R.id.fragment_cardview_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);

        calls = new ArrayList<>();
        callsAdapter = new CallsAdapter(calls);
        recyclerView.setAdapter(callsAdapter);

        onFileUpdate();

        // Call getFile when view created, view will be updated when files downloaded
        if (!V.currentDevice.isMain()) {
            U.getFileAsync(modFile);
        }

        return view;
    }

    @Override
    public String getName() { return "Calls"; }

    @Override
    public String getWatchFile() {
        return modFile;
    }

    @Override
    public void onFileUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                calls = new ArrayList<>();

                if (!readFile() || calls.isEmpty()) {
                    showNoData();
                    return;
                }

                // We need calls from last
                Collections.reverse(calls);

                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideNoData();
                        callsAdapter.update(calls);
                        callsAdapter.notifyDataSetChanged();
                        hideNoData();
                    }
                });
            }
        }).start();
    }

    private boolean readFile() {
        String path = U.getLocalPath(modFile);

        if (!new File(path).exists() || new File(path).length() == 0) {
            return false;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(U.getLocalPath(modFile)));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] pair = line.split(" ");
                if (pair.length < 4)
                    continue;
                Call call = new Call();
                call.date = pair[0];
                call.time  = pair[1];
                call.direction = pair[2];
                call.number = pair[3];
                calls.add(call);
            }
        } catch (IOException e) {
            Log.e(TAG, "Can't read apps file: " + e);
            return false;
        }
        return true;
    }
}
