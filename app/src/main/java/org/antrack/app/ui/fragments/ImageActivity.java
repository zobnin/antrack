package org.antrack.app.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import org.antrack.app.libs.Images;

import app.R;

public class ImageActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Intent i = getIntent();
        String path = i.getExtras().getString("path");

        ImageView imageView = (ImageView) findViewById(R.id.activity_image_image);
        DisplayMetrics metrics = Images.getDisplayMetrics(this);
        Bitmap bitmap = Images.decodeFile(path, metrics.widthPixels);
        imageView.setImageBitmap(bitmap);
    }
}

