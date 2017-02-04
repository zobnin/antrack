package org.antrack.app;

import org.antrack.app.libs.Crypto;
import org.antrack.app.libs.Files;
import org.antrack.app.libs.L;

import java.io.File;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Keys {
    private static final String TAG = "Keys";

    private static PublicKey publicKey;
    private static PrivateKey privateKey;

    public static void saveKeys() {
        if (privateKey == null || publicKey == null) {
            if (!new File(Init.getInstance().MAIN_DIR + C.PUBLIC_KEY_FILE).exists() ||
                    !new File(Init.getInstance().APP_DIR + C.PRIVATE_KEY_FILE).exists()) {
                try {
                    KeyPair keyPair = Crypto.generateKeysRSA();
                    privateKey = keyPair.getPrivate();
                    publicKey = keyPair.getPublic();

                    String stringPrivateKey = Crypto.privateKeyToString(keyPair.getPrivate());
                    String stringPublicKey = Crypto.publicKeyToString(keyPair.getPublic());

                    Files.writeTextFile(Init.getInstance().MAIN_DIR + C.PUBLIC_KEY_FILE, stringPublicKey);
                    Files.writeTextFile(Init.getInstance().APP_DIR + C.PRIVATE_KEY_FILE, stringPrivateKey);
                } catch (Exception e) {
                    L.e(TAG, "Can't write key file: " + e.toString());
                }
            }
        }
    }

    public static PrivateKey getPrivateKey() {
        if (privateKey == null) {
            try {
                String stringKey = Files.readTextFile(Init.getInstance().APP_DIR + C.PRIVATE_KEY_FILE);
                privateKey = Crypto.stringToPrivateKey(stringKey.trim());
            } catch (Exception e) {
                L.e(TAG, "Can't read key file: " + e.toString());
            }
        }
        return privateKey;
    }

}
