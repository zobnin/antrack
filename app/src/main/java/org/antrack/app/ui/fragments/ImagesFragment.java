package org.antrack.app.ui.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import org.antrack.app.libs.Files;
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
        if (V.currentDevice.isMain())
            new File(fullDir).mkdir();

        imageList = new File(fullDir).list();
        if (imageList.length == 0)
            showNodata();

        View view = inflater.inflate(R.layout.fragment_gridview, container, false);

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

        if (!V.currentDevice.isMain()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ArrayList<String> images = U.compareDirs(modDir);

                    if (images == null) {
                        Log.d(TAG, "compareDirs returned null");
                        return;
                    }

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

                // If no files show "No data."
                if (imageList.length == 0) {
                    showNodata();
                    return;
                }

                hideNodata();

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

    protected void showRemoveWarning(final Switch switchHideIcon) {
        String title = getResources().getString(R.string.main_hide_icon_warning_title);
        Spanned text = Html.fromHtml(getResources().getString(R.string.main_hide_icon_warning_text));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(text);

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                U.runCommandAsync("hide on");
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switchHideIcon.setChecked(false);
                dialog.dismiss();
            }
        });

        builder.show();
    }
}

