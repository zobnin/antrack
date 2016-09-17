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

public class CallsFragment extends BaseFragment {
    final String TAG = "CallsFragment";
    Context context;

    private List<Call> calls;

    RecyclerViewAnim recyclerView;
    CallsAdapter callsAdapter;

    String modFile = "/calls";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Otherwise GetActivity() return null after orientation change
        setRetainInstance(true);

        context = getActivity().getApplicationContext();

        View view = inflater.inflate(R.layout.fragment_cardview, null);

        recyclerView = (RecyclerViewAnim) view.findViewById(R.id.fragment_cardview_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);

        calls = new ArrayList<>();
        callsAdapter = new CallsAdapter(calls);
        recyclerView.setAdapter(callsAdapter);

        onFileUpdate();

        // Call getFile when view created, view will be updated when files downloaded
        if (!U.isDeviceMain()) {
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
                readFile();

                if (calls.isEmpty()) {
                    showNodata();
                    return;
                }

                hideNodata();

                if (getActivity() == null) return;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callsAdapter.update(calls);
                        callsAdapter.notifyDataSetChanged();
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
            BufferedReader reader = new BufferedReader(new FileReader(U.getFullPath(modFile)));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] pair = line.split(" ");
                if (pair.length < 4)
                    // FIXME надо выводить картинку, что данных нет
                    return;
                Call call = new Call();
                call.date = pair[0];
                call.time  = pair[1];
                call.direction = pair[2];
                call.number = pair[3];
                calls.add(call);
            }
        } catch (IOException e) {
            Log.e(TAG, "Can't read apps file: " + e);
        }
    }
}
