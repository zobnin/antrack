package org.antrack.app.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.view.View;

import org.antrack.app.libs.Utils;
import org.antrack.app.ui.RecyclerViewAnim;
import org.antrack.app.ui.State;

import app.R;

public class BaseFragment extends Fragment {
    private Thread waitThread;

    public void onFileUpdate() {}
    public String getWatchFile() { return null; }

    protected void waitCardsDrawn(RecyclerViewAnim rv) {
        while (true) {
            if (!rv.mScrollable && !rv.mFirstUpdate) {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    break;
                }
            } else {
                break;
            }
        }
    }

    private void setVisible(View v) {
        v.setAlpha(0);
        v.setVisibility(View.VISIBLE);
        v.animate().alpha(1);
    }

    protected void showNoData() {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() == null) return;
                    if (!State.device.isMain()) {
                        final View loading = getActivity().findViewById(R.id.loading);
                        setVisible(loading);
                        waitThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sleep(15);
                                if (loading.getVisibility() != View.GONE) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            View noData = getActivity().findViewById(R.id.nodata);
                                            setVisible(noData);
                                            loading.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            }
                        });
                        waitThread.start();
                    } else {
                        View noData = getActivity().findViewById(R.id.nodata);
                        setVisible(noData);
                    }
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
                    setVisible(noModule);
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
                    setVisible(noModule);
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
                    setVisible(noModule);
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

    public void hideAllMessages() {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Activity a = getActivity();
                    a.findViewById(R.id.nodata).setVisibility(View.GONE);
                    a.findViewById(R.id.loading).setVisibility(View.GONE);
                    a.findViewById(R.id.nomodule).setVisibility(View.GONE);
                    a.findViewById(R.id.noroot).setVisibility(View.GONE);
                    a.findViewById(R.id.nophone).setVisibility(View.GONE);
                    if (waitThread != null)
                        waitThread.interrupt();
                }
            });
    }
}
