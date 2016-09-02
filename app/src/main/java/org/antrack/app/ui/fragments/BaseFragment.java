package org.antrack.app.ui.fragments;

import android.support.v4.app.Fragment;

public class BaseFragment extends Fragment {
    protected boolean blocked = false;

    protected synchronized boolean isBlocked() {
        return blocked;
    }

    protected synchronized void block() {
        blocked = true;
    }

    protected synchronized void unblock() {
        blocked = false;
    }

    public String getName() { return null; }
    public void onFileUpdate() {}
    public String getWatchFile() { return null; }
}
