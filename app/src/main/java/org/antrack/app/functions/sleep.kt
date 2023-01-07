package org.antrack.app.functions

fun sleep(time: Int) {
    try {
        Thread.sleep((time * 1000).toLong())
    } catch (e: InterruptedException) {
        //
    }
}
