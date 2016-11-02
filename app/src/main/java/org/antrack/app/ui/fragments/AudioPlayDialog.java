package org.antrack.app.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.antrack.app.libs.Media;

import java.util.concurrent.TimeUnit;

import app.R;

class AudioPlayDialog implements SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "AudioPlayDialog";

    private MediaPlayer mp;
    private Activity activity;

    AudioPlayDialog(Activity activity) {
        this.activity = activity;
    }

    public void show(String title, String file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);

        int p = getDpInPixels(activity, 20);

        LinearLayout linear = new LinearLayout(activity);
        linear.setOrientation(LinearLayout.VERTICAL);
        linear.setPadding(p,p,p,p);

        final TextView progress = new TextView(activity);
        progress.setGravity(Gravity.CENTER_HORIZONTAL);
        progress.setText("0");

        final SeekBar seek = new SeekBar(activity);
        seek.setMax((int) Media.getDuration(file));
        seek.setOnSeekBarChangeListener(this);

        linear.addView(progress);
        linear.addView(seek);
        builder.setView(linear);

        builder.setNegativeButton(R.string.stop, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mp.stop();
                dialog.dismiss();
            }
        });

        builder.show();

        play(file);

        Log.d(TAG, "Start playing: " + file);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mp.isPlaying()) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            seek.setProgress(mp.getCurrentPosition() / 1000);
                            progress.setText(String.format("%02d:%02d",
                                    TimeUnit.MILLISECONDS.toMinutes(mp.getCurrentPosition()),
                                    TimeUnit.MILLISECONDS.toSeconds(mp.getCurrentPosition()) %
                                            TimeUnit.MINUTES.toSeconds(1)
                            ));
                        }
                    });
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {}
                }
            }
        }).start();
    }

    private void play(String file) {
        mp = new MediaPlayer();
        try {
            mp.setDataSource(file);
            mp.prepare();
            mp.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int getDpInPixels(Context context, int dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser)
            mp.seekTo(progress * 1000);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
