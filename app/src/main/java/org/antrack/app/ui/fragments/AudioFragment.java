package org.antrack.app.ui.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import org.antrack.app.Pw;
import org.antrack.app.libs.Files;
import org.antrack.app.libs.Media;
import org.antrack.app.ui.RecyclerViewAnim;
import org.antrack.app.ui.U;
import org.antrack.app.ui.V;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import app.R;

public class AudioFragment extends BaseFragment implements SeekBar.OnSeekBarChangeListener {
    private final String TAG = "AudioFragment";
    private final String MOD = "audio";

    private final int MAX_LENGTH = 600;

    private ArrayList<Audio> audios;
    private AudioAdapter audioAdapter;

    static String audioDir;
    static String audioCmd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Otherwise GetActivity() return null after orientation change
        setRetainInstance(true);
        setHasOptionsMenu(true);

        if(!Mod.check(Mod.AUDIO)) {
            showNomodule(Mod.AUDIO);
            return null;
        }

        audioDir = Mod.getFile(MOD);
        audioCmd = Mod.getCommand(MOD);

        View view = inflater.inflate(R.layout.fragment_cardview, null);

        Context context = getActivity().getApplicationContext();
        RecyclerViewAnim recyclerView = (RecyclerViewAnim) view.findViewById(R.id.fragment_cardview_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);

        audios = new ArrayList<>();
        audioAdapter = new AudioAdapter(getActivity(), audios);
        recyclerView.setAdapter(audioAdapter);

        if (!V.currentDevice.isMain()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    new File(U.getLocalPath(audioDir)).mkdir();

                    ArrayList<String> audioFiles = U.compareDirs(audioDir);

                    if (audioFiles == null) {
                        Log.d(TAG, "compareDirs returned null");
                        return;
                    }

                    if (!audioFiles.isEmpty()) {
                        for (String file : audioFiles) {
                            U.getFile(audioDir + file);
                        }
                    }
                }
            }).start();
        }

        onFileUpdate();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_audio, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.toolbar_action_record:
                if (Mod.check(Mod.AUDIO)) {
                    showRecordDialog();
                } else {
                    if (getActivity() != null) {
                        Mod.showNoModule(getActivity(), Mod.AUDIO);
                    }
                }
                return true;
            case R.id.toolbar_action_delete:
                showRemoveDialog();
                return true;
        }
        return false;
    }

    @Override
    public String getName() { return "Audio"; }

    @Override
    public String getWatchFile() {
        return audioDir;
    }

    @Override
    public void onFileUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                audios = new ArrayList<>();

                if(!readFiles() || audios.isEmpty()) {
                    showNodata();
                    return;
                }

                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        audioAdapter.update(audios);
                        audioAdapter.notifyDataSetChanged();
                        hideNodata();
                    }
                });
            }
        }).start();
    }

    private boolean readFiles() {
        String fullDir = U.getLocalPath(audioDir);
        String[] fileList = new File(fullDir).list();

        if (fileList == null || fileList.length == 0) {
            return false;
        }

        audios = new ArrayList<>();

        for (String file : fileList) {
            Audio audio = new Audio();
            audio.file = file;
            audio.length = Media.getDuration(fullDir + file);
            audios.add(audio);
        }
        return true;
    }

    TextView current;

    protected void showRemoveDialog() {
        String title = getResources().getString(R.string.main_hide_icon_warning_title);
        // FIXME translate
        String text = "bla-bla-bla";

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(text);

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Mod.deleteFiles(Mod.AUDIO);
                audioAdapter.update(new ArrayList<Audio>());
                audioAdapter.notifyDataSetChanged();
                showNodata();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    protected void showRecordDialog() {
        // FIXME translate
        String title = "Record audio";
        String text = "How long you want record?";

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(text);

        int p = getDpInPixels(20);

        LinearLayout linear = new LinearLayout(getActivity());
        linear.setOrientation(LinearLayout.VERTICAL);
        linear.setPadding(p,p,p,p);

        current = new TextView(getActivity());
        current.setGravity(Gravity.CENTER_HORIZONTAL);
        current.setText("01:00");

        final SeekBar seek = new SeekBar(getActivity());
        seek.setMax(MAX_LENGTH);
        seek.setProgress(60);
        seek.setOnSeekBarChangeListener(this);

        linear.addView(current);
        linear.addView(seek);
        builder.setView(linear);

        // FIXME translate
        builder.setPositiveButton("Record", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String seconds = String.valueOf(seek.getProgress());
                U.runCommandAsync(audioCmd + " " + seconds);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private int getDpInPixels(int dp) {
        float scale = getActivity().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        current.setText(String.format("%02d:%02d",
                TimeUnit.SECONDS.toMinutes(progress), progress % 60)
        );
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
