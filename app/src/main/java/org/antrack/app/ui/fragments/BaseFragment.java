package org.antrack.app.ui.fragments;

import android.support.v4.app.Fragment;
import android.view.View;

import org.antrack.app.ui.V;

import app.R;

public class BaseFragment extends Fragment {
    public String getName() { return null; }
    public void onFileUpdate() {}
    public String getWatchFile() { return null; }

    protected void showNoData() {
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

    protected void hideNoData() {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getActivity().findViewById(R.id.nodata).setVisibility(View.GONE);
                }
            });
    }

    // FIXME Module name
    protected void showNoModule(String modName) {
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

    protected void hideNoModule() {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getActivity().findViewById(R.id.nomodule).setVisibility(View.GONE);
                }
            });
    }

    protected void showNoRoot() {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() == null) return;
                    View noModule = getActivity().findViewById(R.id.noroot);
                    noModule.setAlpha(0);
                    noModule.setVisibility(View.VISIBLE);
                    noModule.animate().alpha(1);
                }
            });
    }

    protected void hideNoRoot() {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getActivity().findViewById(R.id.noroot).setVisibility(View.GONE);
                }
            });
    }

    protected void showNoPhone() {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() == null) return;
                    View noModule = getActivity().findViewById(R.id.nophone);
                    noModule.setAlpha(0);
                    noModule.setVisibility(View.VISIBLE);
                    noModule.animate().alpha(1);
                }
            });
    }

    protected void hideNoPhone() {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getActivity().findViewById(R.id.nophone).setVisibility(View.GONE);
                }
            });
    }

    public void hideAll() {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getActivity().findViewById(R.id.nodata).setVisibility(View.GONE);
                    getActivity().findViewById(R.id.nomodule).setVisibility(View.GONE);
                    getActivity().findViewById(R.id.noroot).setVisibility(View.GONE);
                    getActivity().findViewById(R.id.nophone).setVisibility(View.GONE);
                }
            });
    }
}
