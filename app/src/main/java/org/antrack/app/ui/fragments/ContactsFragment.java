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

public class ContactsFragment extends BaseFragment {
    private final String TAG = "AppsFragment";

    private List<Contact> contacts;

    private RecyclerViewAnim recyclerView;
    private ContactsAdapter contactsAdapter;

    private String contactsFile = "/contacts";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Otherwise GetActivity() return null after orientation change
        setRetainInstance(true);

        View view = inflater.inflate(R.layout.fragment_cardview, null);

        Context context = getActivity().getApplicationContext();
        recyclerView = (RecyclerViewAnim) view.findViewById(R.id.fragment_cardview_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);

        contacts = new ArrayList<>();
        contactsAdapter = new ContactsAdapter(contacts);
        recyclerView.setAdapter(contactsAdapter);

        onFileUpdate();

        U.runCommandAsync("contacts");
        if (!U.isDeviceMain()) {
            U.getFileAsync(contactsFile);
        }

        return view;
    }

    @Override
    public String getName() { return "Contacts"; }

    @Override
    public String getWatchFile() {
        return contactsFile;
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
                        contactsAdapter.update(contacts);
                        contactsAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    private void readFile() {
        String path = U.getFullPath(contactsFile);

        if (!new File(path).exists()) {
            return;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(U.getFullPath(contactsFile)));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] pair = line.split(":");
                if (pair.length < 2)
                    // FIXME надо выводить картинку, что данных нет
                    return;
                Contact contact = new Contact();
                contact.name = pair[0];
                contact.number  = pair[1].trim();
                contacts.add(contact);
                // FIXME добавить сохранение иконки в кеш
            }
        } catch (IOException e) {
            Log.e(TAG, "Can't read apps file: " + e);
        }
    }
}
