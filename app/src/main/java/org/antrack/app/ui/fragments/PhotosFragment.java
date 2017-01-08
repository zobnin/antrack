package org.antrack.app.ui.fragments;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.antrack.app.ui.U;

import app.R;

public class PhotosFragment extends ImagesFragment {
    @Override
    public String getModule() { return Mod.CAMERA; }

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
                if (Mod.check(getModule())) {
                    U.runCommandAsync(getModule() + " back");
                } else {
                    Mod.showNoModule(getActivity(), getModule());
                }
                return true;
            case R.id.toolbar_action_front_camera:
                if (Mod.check(getModule())) {
                    U.runCommandAsync(getModule() + " front");
                } else {
                    Mod.showNoModule(getActivity(), getModule());
                }
                return true;
            case R.id.toolbar_action_delete:
                showRemoveDialog();
                return true;
        }
        return false;
    }
}
