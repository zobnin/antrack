package org.antrack.app.ui.fragments

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import app.R
import org.antrack.app.libs.L
import org.antrack.app.libs.Media
import org.antrack.app.ui.RecyclerViewAnim
import org.antrack.app.ui.State
import org.antrack.app.ui.U
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class AudioFragment : BaseFragment(), SeekBar.OnSeekBarChangeListener {
    private val TAG = "AudioFragment"
    private val MAX_LENGTH = 600

    override val module = "audio"

    lateinit private var executor: ExecutorService
    lateinit private var audioAdapter: AudioAdapter
    lateinit private var recyclerView: RecyclerViewAnim

    private var audios: ArrayList<Audio>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Otherwise GetActivity() return null after orientation change
        retainInstance = true
        setHasOptionsMenu(true)

        checkModule() || return null

        val view = inflater.inflate(R.layout.fragment_cardview, container, false)

        val context = activity.applicationContext
        recyclerView = view.findViewById(R.id.fragment_cardview_list) as RecyclerViewAnim
        val linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager

        audios = ArrayList<Audio>()
        audioAdapter = AudioAdapter(activity, audios)
        recyclerView.adapter = audioAdapter

        executor = Executors.newFixedThreadPool(1)

        if (!State.device.isMain) {
            Thread(Runnable {
                File(U.getLocalPath(watchFile!!)).mkdir()

                val audioFiles = U.compareDirs(watchFile as String)

                if (audioFiles == null) {
                    L.d(TAG, "compareDirs returned null")
                    return@Runnable
                }
                /*
                if (!audioFiles.isEmpty()) {
                    for (file in audioFiles) {
                        U.getFile(watchFile + file)
                    }
                }
                */

                audioFiles.forEach { U.getFile(watchFile + it) }
            }).start()
        }

        onFileUpdate()

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_audio, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.toolbar_action_record -> {
                if (checkModule()) {
                    showRecordDialog()
                } else {
                    if (activity != null) {
                        showNoModuleToast()
                    }
                }
                return true
            }
            R.id.toolbar_action_delete -> {
                showRemoveDialog()
                return true
            }
        }
        return false
    }

    override fun onFileUpdate() {
        executor.submit(Runnable {
            waitCardsDrawn(recyclerView)

            audios = ArrayList<Audio>()

            if (!readFiles() || audios!!.isEmpty()) {
                showNoDataOrLoading()
                return@Runnable
            }

            val activity = activity ?: return@Runnable

            activity.runOnUiThread {
                audioAdapter.update(audios!!)
                audioAdapter.notifyDataSetChanged()
                hideAllMessages()
            }
        })
    }

    private fun readFiles(): Boolean {
        val fullDir = U.getLocalPath(watchFile!!)
        val fileList = File(fullDir).list()

        if (fileList == null || fileList.isEmpty()) {
            return false
        }

        audios = ArrayList<Audio>()

        for (file in fileList) {
            val audio = Audio()
            audio.file = file
            audio.length = Media.getDuration(fullDir + file)
            audios!!.add(audio)
        }
        return true
    }

    internal lateinit var current: TextView

    private fun showRemoveDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.warning)
        builder.setMessage(R.string.delete_files_warning)

        builder.setPositiveButton(R.string.yes) { dialog, _ ->
            deleteFiles()
            audioAdapter.update(ArrayList<Audio>())
            audioAdapter.notifyDataSetChanged()
            showNoData()
            dialog.dismiss()
        }

        builder.setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }

        builder.show()
    }

    private fun showRecordDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.record_audio_title)
        builder.setMessage(R.string.record_audio_text)

        val p = getDpInPixels(20)

        val linear = LinearLayout(activity)
        linear.orientation = LinearLayout.VERTICAL
        linear.setPadding(p, p, p, p)

        current = TextView(activity)
        current.gravity = Gravity.CENTER_HORIZONTAL
        current.text = "01:00"

        val seek = SeekBar(activity)
        seek.max = MAX_LENGTH
        seek.progress = 60
        seek.setOnSeekBarChangeListener(this)

        linear.addView(current)
        linear.addView(seek)
        builder.setView(linear)

        builder.setPositiveButton(R.string.record) { dialog, _ ->
            val seconds = seek.progress.toString()
            U.runCommandAsync(command + " " + seconds)
            dialog.dismiss()
        }

        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }

        builder.show()
    }

    private fun getDpInPixels(dp: Int): Int {
        val scale = activity.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        current.text = String.format("%02d:%02d",
                TimeUnit.SECONDS.toMinutes(progress.toLong()), progress % 60)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    override fun onStopTrackingTouch(seekBar: SeekBar) {}

}
