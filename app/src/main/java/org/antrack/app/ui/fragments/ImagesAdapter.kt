package org.antrack.app.ui.fragments

import android.app.Activity
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import app.R
import org.antrack.app.libs.Images

class ImagesAdapter(
        private val activity: Activity,
        private val imageDir: String,
        private var images: Array<String>?) : BaseAdapter() {

    private val mInflater: LayoutInflater = LayoutInflater.from(activity)
    private val metrics: DisplayMetrics = Images.getDisplayMetrics(activity)

    fun update(images: Array<String>) {
        this.images = images
    }

    override fun getCount(): Int {
        return images!!.size
    }

    override fun getItem(position: Int): Any {
        return images!![position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    // create a new ImageView for each item referenced by the Adapter
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        val frame: FrameLayout
        val image: ImageView
        val text: TextView

        if (v == null) {
            v = mInflater.inflate(R.layout.gridview_images, parent, false)
            v!!.setTag(R.id.gridview_images_frame, v.findViewById(R.id.gridview_images_frame))
            v.setTag(R.id.gridview_images_image, v.findViewById(R.id.gridview_images_image))
            v.setTag(R.id.gridview_images_text, v.findViewById(R.id.gridview_images_text))
        }

        frame = v.getTag(R.id.gridview_images_frame) as FrameLayout
        image = v.getTag(R.id.gridview_images_image) as ImageView
        text = v.getTag(R.id.gridview_images_text) as TextView

        Thread(Runnable {
            val bitmap = Images.decodeFile(imageDir + images!![position], metrics.widthPixels / 2)

            val a = images!![position].substring(0, images!![position].lastIndexOf('.')).split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val imageText = a[0] + "." + a[1] + "." + a[2] + " " + a[3] + ":" + a[4] + ":" + a[5]

            activity.runOnUiThread {
                image.setImageBitmap(bitmap)
                text.text = imageText
                if (convertView == null) {
                    frame.alpha = 0f
                    frame.animate().alpha(1f)
                }
            }
        }).start()

        return v
    }

}

