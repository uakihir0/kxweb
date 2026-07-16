package work.socialhub.kxweb.util

import kotlin.random.Random

/**
 * OAuth1 signature generation utility.
 * Implements OAuth 1.0a HMAC-SHA1 signing for X (Twitter) API requests.
 *
 * Reference: Nitter's OAuth implementation (apiutils.nim)
 */
expect object OAuth1Util {

    /**
     * Generate OAuth1 Authorization header value.
     *
     * @param method HTTP method (GET, POST)
     * @param url Full request URL (without query parameters)
     * @param queryParams Query parameters as key-value pairs
     * @param consumerKey OAuth consumer key
     * @param consumerSecret OAuth consumer secret
     * @param oauthToken OAuth access token
     * @param oauthSecret OAuth access token secret
     * @return Authorization header value (e.g., "OAuth oauth_consumer_key=..., ...")
     */
    fun generateAuthHeader(
        method: String,
        url: String,
        queryParams: Map<String, String>,
        consumerKey: String,
        consumerSecret: String,
        oauthToken: String,
        oauthSecret: String,
    ): String
}

/**
 * Percent-encode a string according to RFC 3986.
 */
internal fun percentEncode(value: String): String {
    return buildString {
        for (char in value.encodeToByteArray()) {
            val c = char.toInt() and 0xFF
            if (c in 0x30..0x39 || c in 0x41..0x5A || c in 0x61..0x7A ||
                c == 0x2D || c == 0x2E || c == 0x5F || c == 0x7E
            ) {
                append(c.toChar())
            } else {
                append('%')
                append(c.toString(16).uppercase().padStart(2, '0'))
            }
        }
    }
}

/**
 * Generate a random nonce string.
 */
internal fun generateNonce(): String {
    val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return buildString {
        repeat(32) {
            append(chars[Random.nextInt(chars.length)])
        }
    }
}

/**
 * Get current timestamp as seconds since epoch.
 */
internal fun currentTimestamp(): String {
    return kotlin.time.Clock.System.now().epochSeconds.toString()
}
