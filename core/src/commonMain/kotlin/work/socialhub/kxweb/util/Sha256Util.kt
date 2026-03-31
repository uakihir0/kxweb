package work.socialhub.kxweb.util

/**
 * SHA-256 hash utility.
 * Platform-specific implementations:
 * - JVM: java.security.MessageDigest
 * - JS/Native: UnsupportedOperationException
 */
expect object Sha256Util {
    fun hash(input: ByteArray): ByteArray
}
