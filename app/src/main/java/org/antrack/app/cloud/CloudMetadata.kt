package org.antrack.app.cloud

open class CloudMetadata(
    open val path: String,
    open val name: String,
)

class CloudFileMetadata(
    override val path: String,
    override val name: String,
    val lastModified: Long,
    val size: Long,
    val hash: String,
    val revision: String,
) : CloudMetadata(path, name)