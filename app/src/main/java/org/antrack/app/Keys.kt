package org.antrack.app

import org.antrack.app.libs.Crypto
import org.antrack.app.libs.Files
import org.antrack.app.libs.L
import java.io.File
import java.security.PrivateKey
import java.security.PublicKey

object Keys {
    private val TAG = "Keys"

    private var publicKey: PublicKey? = null
    private var privateKey: PrivateKey? = null

    fun saveKeys() {
        if (privateKey == null || publicKey == null) {
            if (!File(Init.MAIN_DIR + C.PUBLIC_KEY_FILE).exists() || !File(Init.APP_DIR + C.PRIVATE_KEY_FILE).exists()) {
                try {
                    val keyPair = Crypto.generateKeysRSA()
                    privateKey = keyPair.private
                    publicKey = keyPair.public

                    val stringPrivateKey = Crypto.privateKeyToString(keyPair.private)
                    val stringPublicKey = Crypto.publicKeyToString(keyPair.public)

                    Files.writeTextFile(Init.MAIN_DIR + C.PUBLIC_KEY_FILE, stringPublicKey)
                    Files.writeTextFile(Init.APP_DIR + C.PRIVATE_KEY_FILE, stringPrivateKey)
                } catch (e: Exception) {
                    L.e(TAG, "Can't write key file: " + e.toString())
                }

            }
        }
    }

    fun getPrivateKey(): PrivateKey? {
        if (privateKey == null) {
            try {
                val stringKey = Files.readTextFile(Init.APP_DIR + C.PRIVATE_KEY_FILE)
                privateKey = Crypto.stringToPrivateKey(stringKey.trim { it <= ' ' })
            } catch (e: Exception) {
                L.e(TAG, "Can't read key file: " + e.toString())
            }

        }
        return privateKey
    }

}
