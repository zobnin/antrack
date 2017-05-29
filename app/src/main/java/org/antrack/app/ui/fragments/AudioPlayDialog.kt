package org.antrack.app.ui.fragments

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.support.v7.app.AlertDialog
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import app.R
import org.antrack.app.libs.L
import org.antrack.app.libs.Media
import java.util.*
import java.util.concurrent.TimeUnit

internal class AudioPlayDialog(private val activity: Activity) : SeekBar.OnSeekBarChangeListener {

    private var mp: MediaPlayer? = null
    private var seek: SeekBar? = null
    private var progress: TextView? = null
    private var file: String? = null

    fun show(title: String, file: String) {
        this.file = file

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(title)

        val p = getDpInPixels(activity, 20)

        val linear = LinearLayout(activity)
        linear.orientation = LinearLayout.VERTICAL
        linear.setPadding(p, p, p, p)

        progress = TextView(activity)
        progress!!.gravity = Gravity.CENTER_HORIZONTAL
        progress!!.text = "0"

        seek = SeekBar(activity)
        seek!!.max = Media.getDuration(file).toInt()
        seek!!.setOnSeekBarChangeListener(this)

        linear.addView(progress)
        linear.addView(seek)
        builder.setView(linear)

        builder.setNegativeButton(R.string.stop) { dialog, which ->
            mp!!.stop()
            dialog.dismiss()
        }

        builder.setOnCancelListener { mp!!.stop() }

        builder.show()

        play(this.file as String)

        L.d(TAG, "Start playing: " + file)

    }

    private fun play(file: String) {
        mp = MediaPlayer()
        try {
            mp!!.setDataSource(file)
            mp!!.prepare()
            mp!!.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Thread(Runnable {
            while (mp!!.isPlaying) {
                activity.runOnUiThread {
                    seek!!.progress = mp!!.currentPosition / 1000
                    progress!!.text = String.format(Locale.US, "%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(mp!!.currentPosition.toLong()),
                            TimeUnit.MILLISECONDS.toSeconds(mp!!.currentPosition.toLong()) % TimeUnit.MINUTES.toSeconds(1)
                    )
                }
                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                }

            }
        }).start()
    }

    private fun getDpInPixels(context: Context, dp: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            if (mp!!.isPlaying) {
                mp!!.seekTo(progress * 1000)
            } else {
                play(file as String)
                mp!!.seekTo(progress * 1000)
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    override fun onStopTrackingTouch(seekBar: SeekBar) {}

    companion object {
        private val TAG = "AudioPlayDialog"
    }
}
