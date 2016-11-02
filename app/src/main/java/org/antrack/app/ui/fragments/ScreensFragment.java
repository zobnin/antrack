package org.antrack.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.antrack.app.ui.State;
import org.antrack.app.ui.U;

import app.R;

public class ScreensFragment extends ImagesFragment {
    @Override
    public String getMod() { return Mod.SCREENSHOT; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!State.features.root) {
            showNoRoot();
            return null;
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_screens, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.toolbar_action_front_camera:
                if (Mod.check(getMod())) {
                    U.runCommandAsync(getMod());
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
