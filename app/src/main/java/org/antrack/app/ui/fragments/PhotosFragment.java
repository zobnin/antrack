package org.antrack.app.ui.fragments;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.antrack.app.ui.U;

import app.R;

public class PhotosFragment extends ImagesFragment {
    @Override
    public String getMod() { return "camera"; }
    @Override
    public String getName() { return "Photos"; }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_photos, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.toolbar_action_back_camera:
                U.runCommandAsync("camera back");
                return true;

            case R.id.toolbar_action_front_camera:
                U.runCommandAsync("camera front");
                return true;
        }
        return false;
    }
}
