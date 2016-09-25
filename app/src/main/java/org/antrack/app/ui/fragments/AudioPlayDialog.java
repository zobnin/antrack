package org.antrack.app.ui.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.antrack.app.libs.Media;
import org.antrack.app.libs.Utils;

public class AudioPlayDialog {
    private static MediaPlayer mp;

    public static void show(final Activity activity, String title, String file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);

        LinearLayout linear = new LinearLayout(activity);
        linear.setOrientation(LinearLayout.VERTICAL);

        final TextView progress = new TextView(activity);
        progress.setGravity(Gravity.CENTER_HORIZONTAL);
        progress.setText("0");

        final SeekBar seek = new SeekBar(activity);
        seek.setMax((int) Media.getDuration(file));

        linear.addView(progress);
        linear.addView(seek);
        builder.setView(linear);

        // FIXME translate
        /*
        builder.setPositiveButton("Record", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String seconds = String.valueOf(seek.getProgress());
                U.runCommandAsync("audio " + seconds);
                dialog.dismiss();
            }
        });
        */
        builder.setNegativeButton("Stop", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mp.stop();
                dialog.dismiss();
            }
        });

        builder.show();

        play(file);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mp.isPlaying()) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            seek.setProgress(mp.getCurrentPosition() / 1000);
                            progress.setText(String.valueOf(mp.getCurrentPosition() / 1000));
                        }
                    });
                    Utils.sleep(1);
                }
            }
        }).start();
    }

    private static void play(String file) {
        mp = new MediaPlayer();
        try {
            mp.setDataSource(file);
            mp.prepare();
            mp.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
