package org.antrack.app.functions

import java.io.*

fun File.touch() {
    try {
        mkdirsForFile()
        createNewFile()
    } catch (e: IOException) {
        //
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

fun File.addLine(text: String) {
    PrintWriter(BufferedWriter(FileWriter(this, true))).use {
        it.println(text)
    }
}

fun File.purgeDir() {
    try {
        for (file in listFiles()) file.delete()
    } catch (e: Exception) {
        //
    }
}
