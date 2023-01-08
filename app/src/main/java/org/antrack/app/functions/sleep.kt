package org.antrack.app.functions

fun sleepS(time: Int) {
    try {
        Thread.sleep((time * 1000).toLong())
    } catch (e: InterruptedException) {
        //
    }
}

fun sleep(time: Long) {
    try {
        Thread.sleep(time)
    } catch (e: InterruptedException) {
        //
    }
}
