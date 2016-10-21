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

public class SmsFragment extends BaseFragment {
    final String TAG = "AppsFragment";
    Context context;

    private List<Sms> smses;
    SmsAdapter smsAdapter;

    String smsDir;
    String smsCmd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Otherwise GetActivity() return null after orientation change
        setRetainInstance(true);

        if (!Mod.check(Mod.DUMPSMS)) {
            showNoModule(Mod.DUMPSMS);
            return null;
        }

        smsDir = Mod.getFile(Mod.DUMPSMS);
        smsCmd = Mod.getCommand(Mod.DUMPSMS);

        context = getActivity().getApplicationContext();

        View view = inflater.inflate(R.layout.fragment_cardview, container, false);

        RecyclerViewAnim recyclerView = (RecyclerViewAnim) view.findViewById(R.id.fragment_cardview_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);

        smses = new ArrayList<>();
        smsAdapter = new SmsAdapter(smses);
        recyclerView.setAdapter(smsAdapter);

        onFileUpdate();

        U.runCommandAsync(smsCmd);

        if (!V.currentDevice.isMain()) {
            // FIXME
            U.getFileAsync(smsDir + "inbox");
        }

        return view;
    }

    @Override
    public String getName() { return "SMS"; }

    @Override
    public String getWatchFile() {
        return smsDir;
    }

    @Override
    public void onFileUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                smses = new ArrayList<>();

                if (!readFile() || smses.isEmpty()) {
                    showNoData();
                    return;
                }

                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        smsAdapter.update(smses);
                        smsAdapter.notifyDataSetChanged();
                        hideNoData();
                    }
                });
            }
        }).start();
    }

    private boolean readFile() {
        // FIXME
        String path = U.getLocalPath(smsDir + "inbox");

        if (!new File(path).exists()) {
            return false;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(U.getLocalPath(smsDir + "inbox")));
            String line;
            Sms sms = new Sms();
            while ((line = reader.readLine()) != null) {
                String[] pair = line.split(":");
                switch(pair[0]) {
                    case "From":
                        sms.from = line.replaceAll(pair[0] + ":", "").trim();
                        break;
                    case "Date":
                        sms.date = line.replaceAll(pair[0] + ":", "").trim();
                        break;
                    case "Body":
                        sms.body = line.replaceAll(pair[0] + ":", "").trim();
                        break;
                    default:
                        smses.add(sms);
                        sms = new Sms();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Can't read sms file: " + e);
            return false;
        }
        return true;
    }
}
