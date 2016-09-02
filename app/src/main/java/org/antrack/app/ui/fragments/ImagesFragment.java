package org.antrack.app.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.antrack.app.ui.U;
import org.antrack.app.ui.V;

import java.io.File;
import java.util.ArrayList;

import app.R;

public class ImagesFragment extends BaseFragment {
    final private String TAG = "ImagesFragment";

    private String modDir;

    private String fullDir;
    private String[] imageList;

    ImagesAdapter imagesAdapter;
    GridView gridview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Otherwise GetActivity() return null after orientation change
        setRetainInstance(true);

        setHasOptionsMenu(true);

        if (!V.modules.containsKey(getMod())) {
            // FIXME создавать view с сообщением о ненайденном модуле
            return null;
        }

        modDir = V.modules.get(getMod()).result;

        fullDir = U.getFullPath(modDir);
        imageList = new File(fullDir).list();

        View view = inflater.inflate(R.layout.fragment_gridview, null);

        gridview = (GridView) view.findViewById(R.id.fragment_gridview);
        imagesAdapter = new ImagesAdapter(getActivity(), U.getFullPath(modDir), imageList);
        gridview.setAdapter(imagesAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Intent i = new Intent(getActivity(), ImageActivity.class);
                i.putExtra("path", fullDir + imageList[position]);
                startActivity(i);
            }
        });

        if (!U.isDeviceMain()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ArrayList<String> images = U.compareDirs(modDir);
                    if (!images.isEmpty()) {
                        for (String image : images) {
                            U.getFile(modDir + image);
                        }
                    }
                }
            }).start();
        }

        return view;
    }

    public String getMod() { return null; }

    @Override
    public String getName() {
        return "Screenshots";
    }

    @Override
    public String getWatchFile() {
        return modDir;
    }

    @Override
    public void onFileUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Re-read image list to reflect changes
                imageList = new File(fullDir).list();

                if (getActivity() == null) return;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imagesAdapter.update(imageList);
                        imagesAdapter.notifyDataSetChanged();
                        gridview.smoothScrollToPosition(imageList.length-1);
                    }
                });
            }
        }).start();
    }
}

