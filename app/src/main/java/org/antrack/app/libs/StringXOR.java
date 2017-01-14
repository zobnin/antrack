package org.antrack.app.libs;

import android.util.Base64;

public class StringXOR {
    public static String encode(String s, String key) {
        return Base64.encodeToString(xor(s.getBytes(), key.getBytes()), 0);
    }

    public static String decode(String s, String key) {
        return new String(xor(Base64.decode(s, 0), key.getBytes()));
    }

    private static byte[] xor(byte[] a, byte[] key) {
        byte[] out = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = (byte) (a[i] ^ key[i%key.length]);
        }
        return out;
    }
}
