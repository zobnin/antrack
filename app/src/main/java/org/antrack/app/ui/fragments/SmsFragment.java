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
import java.util.Arrays;
import java.util.List;

import app.R;

public class SmsFragment extends BaseFragment {
    final String TAG = "AppsFragment";
    Context context;

    private List<Sms> smses;

    RecyclerViewAnim recyclerView;
    SmsAdapter smsAdapter;

    String smsFile = "/sms/inbox";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Otherwise GetActivity() return null after orientation change
        setRetainInstance(true);

        context = getActivity().getApplicationContext();

        View view = inflater.inflate(R.layout.fragment_cardview, null);

        recyclerView = (RecyclerViewAnim) view.findViewById(R.id.fragment_cardview_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);

        smses = new ArrayList<>();
        smsAdapter = new SmsAdapter(smses);
        recyclerView.setAdapter(smsAdapter);

        onFileUpdate();

        U.runCommandAsync("dumpsms");

        if (!U.isDeviceMain()) {
            U.getFileAsync(smsFile);
        }

        return view;
    }

    @Override
    public String getName() { return "SMS"; }

    @Override
    public String getWatchFile() {
        return smsFile;
    }

    @Override
    public void onFileUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                readFile();

                if (getActivity() == null) return;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        smsAdapter.update(smses);
                        smsAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    private void readFile() {
        String path = U.getFullPath(smsFile);

        if (!new File(path).exists()) {
            return;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(U.getFullPath(smsFile)));
            String line;
            Sms sms = new Sms();
            while ((line = reader.readLine()) != null) {
                String[] pair = line.split(":");
                switch(pair[0]) {
                    case "From":
                        sms.from = pair[1].trim();
                        break;
                    case "Date":
                        sms.date = pair[1].trim() + ":" + pair[2] + ":" + pair[3];
                        break;
                    case "Body":
                        sms.body = pair[1].trim();
                        break;
                    default:
                        smses.add(sms);
                        sms = new Sms();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Can't read sms file: " + e);
        }
    }
}
