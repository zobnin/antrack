package org.antrack.app.ui.fragments

import android.os.Bundle
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import app.R

class HowtoFragment : BaseFragment() {
    override val module = ""
    override val needSubtitle = false
    override fun onFileUpdate() {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_textview, null)

        val textView = view.findViewById(R.id.fragment_textview_text) as TextView
        textView.text = Html.fromHtml(getString(R.string.help_text))
        textView.movementMethod = ScrollingMovementMethod()

        textView.alpha = 0f
        textView.animate().alpha(1f)

        return view
    }
}
