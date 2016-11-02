package org.antrack.app.ui.fragments;

import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import app.R;

public class HowtoFragment extends BaseFragment {
    final String TAG = "InfoFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_textview, null);

        TextView textView = (TextView) view.findViewById(R.id.fragment_textview_text);
        textView.setText(Html.fromHtml(getString(R.string.help_text)));
        textView.setMovementMethod(new ScrollingMovementMethod());

        textView.setAlpha(0);
        textView.animate().alpha(1);

        return view;
    }
}
