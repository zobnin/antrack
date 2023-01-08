package org.antrack.app.functions

import android.graphics.Color
import org.antrack.app.FALSE
import org.antrack.app.TRUE

fun CharSequence.highlightBooleans(): CharSequence {
    return replace(" $TRUE", " $TRUE".color(Color.GREEN))
        .replace(" $FALSE", " $FALSE".color(Color.RED))
}
