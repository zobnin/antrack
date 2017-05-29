package org.antrack.app.libs

import android.media.MediaPlayer
import android.media.MediaRecorder

import java.io.FileInputStream
import java.io.IOException

object Media {
    private val TAG = "Media"

    fun recordAudio(file: String, time: Int) {
        val recorder = MediaRecorder()

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        recorder.setOutputFile(file)

        try {
            recorder.prepare()
        } catch (e: IOException) {
            L.e(TAG, "recordAudio exception")
        }

        recorder.start()

        val timer = Thread(Runnable {
            try {
                Thread.sleep((time * 1000).toLong())
            } catch (e: InterruptedException) {
                L.d(TAG, "timer interrupted")
            } finally {
                recorder.stop()
                recorder.release()
            }
        })

        timer.start()
    }

    fun getDuration(path: String): Long {
        var duration: Long
        val mp = MediaPlayer()

        try {
            val stream = FileInputStream(path)
            mp.setDataSource(stream.fd)
            stream.close()
            mp.prepare()
            duration = mp.duration.toLong()
            mp.release()
        } catch (e: IOException) {
            L.e(TAG, "Can't open media file: " + e.toString())
            duration = -1
        }

        return duration / 1000
    }
}
