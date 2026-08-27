package org.antrack.app.tests

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

fun isCorrectDateString(format: String, string: String): Boolean {
    return try {
        SimpleDateFormat(format, Locale.US).parse(string)
        true
    } catch (e: Exception) {
        false
    }
}

fun isFloat(string: String): Boolean {
    return try {
        string.toFloat()
        true
    } catch (e: Exception) {
        false
    }
}

fun genRandomString(): String {
    return UUID.randomUUID().toString()
}
