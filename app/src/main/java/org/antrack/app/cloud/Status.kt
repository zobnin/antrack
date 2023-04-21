package org.antrack.app.cloud

data class Status(
    val isConnected: Boolean,
    val isTokenExpired: Boolean = false,
    val provider: String = "unknown",
    val userId: String = "",
    val email: String = "",
)