package org.antrack.app.functions

import android.content.Context
import org.antrack.app.Env
import java.io.File

fun Context.unpackAsset(fileName: String) {
    val absolutePath = Env.appDirPath + "/" + fileName
    val file = File(absolutePath)
    val iStream = assets.open(fileName)
    val oStream = file.outputStream()

    iStream.copyTo(oStream)
}

