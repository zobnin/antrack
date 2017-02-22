package org.antrack.app.libs;

// http://stackoverflow.com/a/41156/5984995

import java.math.BigInteger;
import java.security.SecureRandom;

public final class SessionIdGenerator {
    private SecureRandom random = new SecureRandom();

    public String nextSessionId() {
        return new BigInteger(130, random).toString(32);
    }
}

