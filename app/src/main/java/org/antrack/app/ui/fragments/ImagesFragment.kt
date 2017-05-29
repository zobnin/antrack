package org.antrack.app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.GridView
import app.R
import org.antrack.app.libs.L
import org.antrack.app.ui.State
import org.antrack.app.ui.U
import java.io.File

abstract class ImagesFragment : BaseFragment() {
    private val TAG = "ImagesFragment"

    private val fullDir: String?
        get() = U.getLocalPath(watchFile!!)

    lateinit var imageList: Array<String>
    lateinit var imagesAdapter: ImagesAdapter
    lateinit var gridView: GridView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Otherwise GetActivity() return null after orientation change
        retainInstance = true
        setHasOptionsMenu(true)

        checkModule() || return null

        imageList = File(fullDir!!).list()
        if (imageList.isEmpty())
            showNoDataOrLoading()

        val view = inflater.inflate(R.layout.fragment_gridview, container, false)

        gridView = view.findViewById(R.id.fragment_gridview) as GridView
        imagesAdapter = ImagesAdapter(activity, U.getLocalPath(watchFile!!), imageList)
        gridView.adapter = imagesAdapter

        gridView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val i = Intent(activity, ImageActivity::class.java)
            i.putExtra("path", fullDir!! + imageList[position])
            startActivity(i)
        }

        if (!State.device.isMain) {
            Thread(Runnable {
                val images = U.compareDirs(watchFile!!)

                if (images == null) {
                    L.d(TAG, "compareDirs returned null")
                    return@Runnable
                }

                if (!images.isEmpty()) {
                    for (image in images) {
                        U.getFile(watchFile!! + image)
                    }
                }
            }).start()
        }

        return view
    }

    override fun onFileUpdate() {
        Thread(Runnable {
            // Re-read image list to reflect changes
            imageList = File(fullDir!!).list()

            // If no files show "No data."
            if (imageList.isEmpty()) {
                showNoData()
                return@Runnable
            }

            if (activity == null) return@Runnable
            activity.runOnUiThread {
                imagesAdapter.update(imageList)
                imagesAdapter.notifyDataSetChanged()
                gridView.smoothScrollToPosition(imageList.size - 1)
                hideAllMessages()
            }
        }).start()
    }

    protected fun showRemoveDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.warning)
        builder.setMessage(R.string.delete_files_warning)

        builder.setPositiveButton(R.string.yes) { dialog, _ ->
            deleteFiles()
            imagesAdapter.update(emptyArray<String>())
            imagesAdapter.notifyDataSetChanged()
            showNoData()
            dialog.dismiss()
        }

        builder.setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }

        builder.show()
    }
}

