package org.antrack.app.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.antrack.app.libs.L;
import org.antrack.app.ui.RecyclerViewAnim;
import org.antrack.app.ui.State;
import org.antrack.app.ui.U;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.R;

public class ContactsFragment extends BaseFragment {
    private final String TAG = "ContactsFragment";

    private ExecutorService executor;

    private List<Contact> contacts;
    private ContactsAdapter contactsAdapter;
    private RecyclerViewAnim recyclerView;

    String contactsFile;
    String contactsCmd;

    @Override
    public String getModule() {
        return Mod.CONTACTS;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Otherwise GetActivity() return null after orientation change
        setRetainInstance(true);

        if (!Mod.check(Mod.CONTACTS)) {
            showNoModule(Mod.CONTACTS);
            return null;
        }

        contactsFile = Mod.getFile(Mod.CONTACTS);
        contactsCmd = Mod.getCommand(Mod.CONTACTS);

        View view = inflater.inflate(R.layout.fragment_cardview, container, false);

        Context context = getActivity().getApplicationContext();
        recyclerView = (RecyclerViewAnim) view.findViewById(R.id.fragment_cardview_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);

        contacts = new ArrayList<>();
        contactsAdapter = new ContactsAdapter(getActivity(), contacts);
        recyclerView.setAdapter(contactsAdapter);

        executor = Executors.newFixedThreadPool(1);

        onFileUpdate();

        U.runCommandAsync(contactsCmd);
        if (!State.device.isMain()) {
            U.getFileAsync(contactsFile);
        }

        return view;
    }

    @Override
    public String getWatchFile() {
        return contactsFile;
    }

    @Override
    public void onFileUpdate() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                waitCardsDrawn(recyclerView);

                contacts = new ArrayList<>();
                readFile();

                if (contacts.isEmpty()) {
                    showNoData();
                    return;
                }

                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        contactsAdapter.update(contacts);
                        contactsAdapter.notifyDataSetChanged();
                        hideAllMessages();
                    }
                });
            }
        });
    }

    private void readFile() {
        String path = U.getLocalPath(contactsFile);

        if (!new File(path).exists()) {
            return;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(U.getLocalPath(contactsFile)));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] pair = line.split(":");
                if (pair.length < 2)
                    continue;
                Contact contact = new Contact();
                contact.name = pair[0];
                contact.number  = pair[1].trim();
                contacts.add(contact);
            }
        } catch (IOException e) {
            L.e(TAG, "Can't read apps file: " + e);
        }
    }
}
