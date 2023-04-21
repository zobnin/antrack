package org.antrack.app.tests

import java.text.SimpleDateFormat
import java.util.*

fun isCorrectDateString(format: String, string: String): Boolean {
    return try {
        SimpleDateFormat(format).parse(string)
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
