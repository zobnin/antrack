package org.antrack.app.tests

interface Test {
    fun before()
    fun after()
    fun run(): List<String>
}