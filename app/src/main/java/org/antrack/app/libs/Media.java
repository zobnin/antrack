package org.antrack.app.libs;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;

public class Media {
    private static String TAG="MediaTools";
    private static MediaRecorder recorder = null;

    public static void recordAudio(String file, final int time) {
        recorder = new MediaRecorder();

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(file);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "recordAudio exception");
        }

        recorder.start();

        Thread timer = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(time * 1000);
                } catch (InterruptedException e) {
                    Log.d(TAG, "timer interrupted");
                } finally {
                    recorder.stop();
                    recorder.release();
                }
            }
        });

        timer.start();
    }

    public static long getDuration(String path) {
        long duration;
        MediaPlayer mp = new MediaPlayer();
        try {
            FileInputStream stream = new FileInputStream(path);
            mp.setDataSource(stream.getFD());
            stream.close();
            mp.prepare();
            duration = mp.getDuration();
            mp.release();
        } catch (IOException e) {
            Log.e(TAG, "Can't open media file: " + e.toString());
            duration = -1;
        }
        return duration / 1000;
    }
}
