package org.antrack.app.modules

data class Module(
    val name: String = "",
    val version: String = "",
    val author: String = "",
    val desc: String = "",
    val command: String = "",
    val usesRoot: String = "",
    val usesAdmin: String = "",
    val result: String = "",
    val startWhen: String = "",
)


