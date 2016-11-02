package org.antrack.app.ui.fragments;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.antrack.app.Pw;
import org.antrack.app.libs.Files;
import org.antrack.app.ui.U;

import java.io.File;

import app.R;

public class PhotosFragment extends ImagesFragment {
    @Override
    public String getMod() { return Mod.CAMERA; }

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
                if (Mod.check(getMod())) {
                    U.runCommandAsync(getMod() + " back");
                } else {
                    Mod.showNoModule(getActivity(), getMod());
                }
                return true;
            case R.id.toolbar_action_front_camera:
                if (Mod.check(getMod())) {
                    U.runCommandAsync(getMod() + " front");
                } else {
                    Mod.showNoModule(getActivity(), getMod());
                }
                return true;
            case R.id.toolbar_action_delete:
                showRemoveDialog();
                return true;
        }
        return false;
    }
}
