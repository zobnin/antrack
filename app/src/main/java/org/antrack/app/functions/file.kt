package org.antrack.app.functions

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

fun File.touch() {
    try {
        mkdirsForFile()
        createNewFile()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun File.mkdirsForFile() {
    File(absolutePath.substring(0, absolutePath.lastIndexOf("/"))).mkdirs()
}

fun File.readAsList(): List<String> {
    BufferedReader(FileReader(this)).use { br ->
        return br.lineSequence().toList()
    }
}
