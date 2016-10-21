package org.antrack.app.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import org.antrack.app.libs.Files;
import org.antrack.app.libs.Keyboard;
import org.antrack.app.libs.Utils;
import org.antrack.app.ui.U;
import org.antrack.app.ui.V;

import java.io.IOException;

import app.R;

public class ShellFragment extends BaseFragment {
    private final String TAG = "ShellFragment";

    Context context;

    EditText editText;
    TextView textView;
    TextView ps1;

    String cmdOut;
    String cmdCmd;

    boolean progress = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Otherwise GetActivity() return null after orientation change
        setRetainInstance(true);

        if (!Mod.check(Mod.CMD)) {
            showNoModule(Mod.CMD);
            return null;
        }

        cmdOut = Mod.getFile(Mod.CMD);
        cmdCmd = Mod.getCommand(Mod.CMD);

        context = getActivity().getApplicationContext();

        View view = inflater.inflate(R.layout.fragment_shell, container, false);
        view.setAlpha(0);
        view.animate().alpha(1);

        // Show keyboard when user clicks on any place
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Keyboard.show(getActivity());
            }
        });

        ps1 = (TextView) view.findViewById(R.id.fragment_shell_ps1);
        String text = V.currentDevice.getName() + "$ ";
        ps1.setText(text);

        editText = (EditText) view.findViewById(R.id.fragment_shell_edittext);
        editText.requestFocus();

        textView = (TextView) view.findViewById(R.id.fragment_shell_textview);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Keyboard.show(getActivity());
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendCommand(editText.getText().toString());
                    progress = false;
                    editText.setText("");
                    textView.setText("");
                    showProgress();
                    handled = true;
                }
                return handled;
            }
        });

        return view;
    }

    private void sendCommand(String cmd) {
        U.runCommandAsync(cmdCmd + cmd);
    }

    private void addText(final String text) {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.append(text);
            }
        });
    }

    private void showProgress() {
        if (V.currentDevice.isMain())
            return;

        progress = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                int seconds = 0;

                while(progress) {
                    addText(".");

                    if (seconds == 60) {
                        addText(" :(");
                        progress = false;
                    }

                    Utils.sleep(1);
                    seconds += 1;
                }
            }
        }).start();
    }

    @Override
    public void onFileUpdate() {
        try {
            final String out = Files.readTextFile(U.getLocalPath(cmdOut));

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progress = false;
                    textView.setAlpha(0);
                    // Remove time stamp
                    textView.setText(out.substring(out.indexOf('\n')+1));
                    textView.animate().alpha(1);
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Can't read cmdout: " + e);
        }
    }

    @Override
    public String getName() { return "Shell"; }

    @Override
    public String getWatchFile() {
        return cmdOut;
    }
}
