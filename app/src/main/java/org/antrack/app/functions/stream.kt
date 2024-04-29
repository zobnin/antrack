package org.antrack.app.functions

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.InputStream

fun InputStream.copyTo(oStream: FileOutputStream) {
    BufferedInputStream(this).use { bis ->
        BufferedOutputStream(oStream).use { bos ->
            val bufSize = 8 * 1024
            val buf = ByteArray(bufSize)
            var len: Int
            while (true) {
                len = bis.read(buf, 0, bufSize)
                if (len < 0) {
                    break
                }
                bos.write(buf, 0, len)
            }
        }
    }
}

