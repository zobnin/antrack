package org.antrack.app.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.antrack.app.C;
import org.antrack.app.libs.L;
import org.antrack.app.ui.RecyclerViewAnim;
import org.antrack.app.ui.State;
import org.antrack.app.ui.U;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.R;

public class SmsFragment extends BaseFragment {
    private final String TAG = "SmsFragment";
    private Context context;

    private ExecutorService executor;

    private List<Sms> smses;
    private SmsAdapter smsAdapter;
    private RecyclerViewAnim recyclerView;

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

        if (!State.features.phone) {
            showNoPhone();
            return null;
        }

        smsDir = Mod.getFile(Mod.DUMPSMS);
        smsCmd = Mod.getCommand(Mod.DUMPSMS);

        context = getActivity().getApplicationContext();

        View view = inflater.inflate(R.layout.fragment_cardview, container, false);

        recyclerView = (RecyclerViewAnim) view.findViewById(R.id.fragment_cardview_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);

        smses = new ArrayList<>();
        smsAdapter = new SmsAdapter(smses);
        recyclerView.setAdapter(smsAdapter);

        executor = Executors.newFixedThreadPool(1);

        onFileUpdate();

        U.runCommandAsync(smsCmd);

        if (!State.device.isMain()) {
            // FIXME
            U.getFileAsync(smsDir + "inbox");
            U.getFileAsync(smsDir + "sent");
        }

        return view;
    }

    @Override
    public String getWatchFile() {
        return smsDir;
    }

    @Override
    public void onFileUpdate() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                waitCardsDrawn(recyclerView);

                smses = new ArrayList<>();

                if (!readFile()) {
                    showNoData();
                    return;
                }

                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        smsAdapter.update(smses);
                        smsAdapter.notifyDataSetChanged();
                        hideAllMessages();
                    }
                });
            }
        });
    }

    public class SmsComaparator implements Comparator<Sms> {
        @Override
        public int compare(Sms o1, Sms o2) {
            Date date1, date2;

            SimpleDateFormat format = new SimpleDateFormat(C.DEFAULT_TIME_FORMAT);
            try {
                date1 = format.parse(o1.date);
                date2 = format.parse(o2.date);
            } catch (Exception e) {
                L.e(TAG, "DateComparator exception: " + e.toString());
                return -1;
            }
            return date1.compareTo(date2);
        }
    }

    private boolean readFile() {
        // FIXME
        String pathIn = U.getLocalPath(smsDir + "inbox");
        String pathOut = U.getLocalPath(smsDir + "sent");

        if (!new File(pathIn).exists() && !new File(pathOut).exists()) {
            return false;
        }

        ReadBox(pathIn, true);
        ReadBox(pathOut, false);

        if (smses.isEmpty()) {
            return false;
        }

        // FIXME DateComparator exception: java.lang.NullPointerException: Attempt to invoke virtual method 'int java.lang.String.length()' on a null object reference
        Collections.sort(smses, new SmsComaparator());
        Collections.reverse(smses);

        return true;
    }

    private boolean ReadBox(String path, final boolean inbox) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
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
                        if (inbox) {
                            sms.direction = "Incoming";
                        } else {
                            sms.direction = "Outgoing";
                        }
                        smses.add(sms);
                        sms = new Sms();
                }
            }
        } catch (IOException e) {
            L.e(TAG, "Can't read sms file: " + e);
            return false;
        }
        return true;
    }
}
