package org.antrack.app.libs

// http://stackoverflow.com/a/41156/5984995

import java.math.BigInteger
import java.security.SecureRandom

class SessionIdGenerator {
    private val random = SecureRandom()

    fun nextSessionId(): String {
        return BigInteger(130, random).toString(32)
    }
}
