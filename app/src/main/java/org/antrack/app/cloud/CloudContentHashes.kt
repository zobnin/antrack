package org.antrack.app.cloud

import java.security.MessageDigest

internal object CloudContentHashes {
    private const val DROPBOX_BLOCK_SIZE = 4 * 1024 * 1024

    fun matchesDropbox(content: ByteArray, remote: CloudFileMetadata): Boolean {
        if (remote.hash.isBlank() || remote.size != content.size.toLong()) return false

        val outerDigest = MessageDigest.getInstance("SHA-256")
        var offset = 0

        while (offset < content.size) {
            val blockSize = minOf(DROPBOX_BLOCK_SIZE, content.size - offset)
            val blockDigest = MessageDigest.getInstance("SHA-256")
            blockDigest.update(content, offset, blockSize)
            outerDigest.update(blockDigest.digest())
            offset += blockSize
        }

        return outerDigest.digest().toHex().equals(remote.hash, ignoreCase = true)
    }

    private fun ByteArray.toHex(): String {
        return joinToString("") { "%02x".format(it) }
    }
}
