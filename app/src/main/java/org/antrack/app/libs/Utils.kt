package org.antrack.app.libs

import android.content.Context
import android.widget.Toast
import org.antrack.app.Init
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    private val TAG = "Utils"

    fun unpackAsset(context: Context, file: String) {
        try {
            val path = Init.APP_DIR + "/" + file

            if (File(path).exists())
                return

            val bis = BufferedInputStream(context.assets.open(file))
            val bos = BufferedOutputStream(FileOutputStream(path))

            val BUF_SIZE = 8 * 1024

            val buf = ByteArray(BUF_SIZE)
            var len: Int
            while (true) {
                len = bis.read(buf, 0, BUF_SIZE)
                if (len < 0) { break }
                bos.write(buf, 0, len)
            }

            bos.close()
            bis.close()
        } catch (e: IOException) {
            L.e(TAG, "unpackAsset: " + e.toString())
        }

    }

    // Not thread safe
    fun showToast(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun date(format: String): String {
        val dateFormat = SimpleDateFormat(format, Locale.US)
        val date = Date()
        return dateFormat.format(date)
    }

    fun sleep(time: Int) {
        try {
            Thread.sleep((time * 1000).toLong())
        } catch (e: InterruptedException) {
        }

    }

    fun StreamToString(`is`: InputStream): String {
        /*
     * To convert the InputStream to String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     */
        val reader = BufferedReader(InputStreamReader(`is`))
        val sb = StringBuilder()

        try {
            for (line in reader.readLines()) {
                sb.append(line + "\n")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                `is`.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        return sb.toString()
    }

    fun arrayToString(ar: Array<String>): String {
        val sb = StringBuilder()
        for (s in ar) {
            sb.append(s)
            sb.append(' ')
        }
        return sb.toString().trim { it <= ' ' }
    }

    fun arrayToStringReverse(ar: Array<String>, del: String): String {
        val sb = StringBuilder()
        for (i in ar.indices.reversed()) {
            sb.append(ar[i])
            sb.append(del)
        }
        return sb.toString().trim { it <= ' ' }
    }
}
