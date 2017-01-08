package org.antrack.app.ui.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.R;

public class SettingsFragment extends BaseFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Fragment settingsFragment = new SettingsFragmentNested();
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.add(R.id.settings_container, settingsFragment).commit();

        view.setAlpha(0);
        view.animate().alpha(1);

        return view;
    }
}
