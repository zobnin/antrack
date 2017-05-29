package org.antrack.app.ui.fragments

import android.app.Activity
import android.os.Bundle
import android.widget.ImageView
import app.R
import org.antrack.app.libs.Images

class ImageActivity : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        val i = intent
        val path = i.extras.getString("path")

        val imageView = findViewById(R.id.activity_image_image) as ImageView
        val metrics = Images.getDisplayMetrics(this)
        val bitmap = Images.decodeFile(path, metrics.widthPixels)
        imageView.setImageBitmap(bitmap)
    }
}

