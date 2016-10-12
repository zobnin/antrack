package org.antrack.app.ui.fragments;

import android.support.v4.app.Fragment;
import android.view.View;

import org.antrack.app.ui.V;

import app.R;

public class BaseFragment extends Fragment {
    public String getName() { return null; }
    public void onFileUpdate() {}
    public String getWatchFile() { return null; }

    protected void showNodata() {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() == null) return;
                    View noData = getActivity().findViewById(R.id.nodata);
                    noData.setAlpha(0);
                    noData.setVisibility(View.VISIBLE);
                    noData.animate().alpha(1);
                }
            });
    }

    protected void hideNodata() {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getActivity().findViewById(R.id.nodata).setVisibility(View.GONE);
                }
            });
    }

    protected void showNomodule(String modName) {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() == null) return;
                    View noModule = getActivity().findViewById(R.id.nomodule);
                    noModule.setAlpha(0);
                    noModule.setVisibility(View.VISIBLE);
                    noModule.animate().alpha(1);
                }
            });
    }

    protected void hideNomodule() {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getActivity().findViewById(R.id.nomodule).setVisibility(View.GONE);
                }
            });
    }
}
