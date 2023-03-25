package org.antrack.app.functions

import app.R
import org.antrack.app.App
import org.antrack.app.FALSE
import org.antrack.app.TRUE

fun CharSequence.highlightBooleans(): CharSequence {
    return replace(" $TRUE", " $TRUE".color(App.context.getColor(R.color.green)))
        .replace(" $FALSE", " $FALSE".color(App.context.getColor(R.color.red)))
}
