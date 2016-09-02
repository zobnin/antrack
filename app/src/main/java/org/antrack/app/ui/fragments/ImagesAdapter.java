package org.antrack.app.ui.fragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.antrack.app.libs.Images;
import org.antrack.app.ui.MainActivity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import app.R;

public class ImagesAdapter extends BaseAdapter {
    private final String TAG = "ImagesAdapter";

    private final LayoutInflater mInflater;

    private FragmentActivity activity;

    private String   imageDir;
    private String[] images;
    private DisplayMetrics metrics;

    public ImagesAdapter(FragmentActivity activity, String imageDir, String[] images) {
        this.activity = activity;
        this.imageDir = imageDir;
        this.images = images;

        metrics = Images.getDisplayMetrics(activity);

        mInflater = LayoutInflater.from(activity);
    }

    public void update(String[] images) {
        this.images = images;
    }

    public int getCount() {
        return images.length;
    }

    public Object getItem(int position) {
        return images[position];
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int position, final View convertView, ViewGroup parent) {
        View v = convertView;
        final FrameLayout frame;
        final ImageView image;
        final TextView text;

        if (v == null) {
            v = mInflater.inflate(R.layout.gridview_images, parent, false);
            v.setTag(R.id.gridview_images_frame, v.findViewById(R.id.gridview_images_frame));
            v.setTag(R.id.gridview_images_image, v.findViewById(R.id.gridview_images_image));
            v.setTag(R.id.gridview_images_text, v.findViewById(R.id.gridview_images_text));
        }

        frame = (FrameLayout) v.getTag(R.id.gridview_images_frame);
        image = (ImageView) v.getTag(R.id.gridview_images_image);
        text = (TextView) v.getTag(R.id.gridview_images_text);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = Images.decodeFile(imageDir + images[position], metrics.widthPixels / 2);

                String a[] = images[position].substring(0, images[position].lastIndexOf('.')).split("-");
                final String imageText = a[0]+"."+a[1]+"."+a[2]+" "+a[3]+":"+a[4]+":"+a[5];

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        image.setImageBitmap(bitmap);
                        text.setText(imageText);
                        if (convertView == null) {
                            frame.setAlpha(0);
                            frame.animate().alpha(1);
                        }
                    }
                });
            }
        }).start();

        return v;
    }

}

