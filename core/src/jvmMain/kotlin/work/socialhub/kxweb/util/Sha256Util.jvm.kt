package work.socialhub.kxweb.util

import java.security.MessageDigest

actual object Sha256Util {
    actual fun hash(input: ByteArray): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(input)
    }
}
