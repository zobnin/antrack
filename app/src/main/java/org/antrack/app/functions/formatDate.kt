package org.antrack.app.functions

import java.text.SimpleDateFormat
import java.util.*

fun formatDate(date: Long, format: String): String {
    return SimpleDateFormat(format, Locale.US).format(date)
}

