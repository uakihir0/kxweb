package work.socialhub.kxweb.util

actual object Sha256Util {
    actual fun hash(input: ByteArray): ByteArray {
        throw UnsupportedOperationException(
            "SHA-256 is not yet supported in JavaScript. " +
                "Client transaction ID generation requires JVM or Native."
        )
    }
}
