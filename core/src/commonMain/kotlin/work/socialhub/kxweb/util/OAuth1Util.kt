package work.socialhub.kxweb.util

import org.kotlincrypto.macs.hmac.sha1.HmacSHA1
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

/**
 * OAuth1 signature generation utility.
 * Implements OAuth 1.0a HMAC-SHA1 signing for X (Twitter) API requests.
 *
 * Reference: Nitter's OAuth implementation (apiutils.nim)
 */
object OAuth1Util {

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
    @OptIn(ExperimentalEncodingApi::class)
    fun generateAuthHeader(
        method: String,
        url: String,
        queryParams: Map<String, String>,
        consumerKey: String,
        consumerSecret: String,
        oauthToken: String,
        oauthSecret: String,
    ): String {
        val nonce = generateNonce()
        val timestamp = currentTimestamp()

        // OAuth parameters
        val oauthParams = mapOf(
            "oauth_consumer_key" to consumerKey,
            "oauth_nonce" to nonce,
            "oauth_signature_method" to "HMAC-SHA1",
            "oauth_timestamp" to timestamp,
            "oauth_token" to oauthToken,
            "oauth_version" to "1.0",
        )

        // All parameters (query + oauth) sorted by key
        val allParams = (queryParams + oauthParams).entries.sortedBy { it.key }

        // Build parameter string
        val paramString = allParams.joinToString("&") { (key, value) ->
            "${percentEncode(key)}=${percentEncode(value)}"
        }

        // Build signature base string
        val signatureBase = "${method.uppercase()}&${percentEncode(url)}&${percentEncode(paramString)}"

        // Build signing key
        val signingKey = "${percentEncode(consumerSecret)}&${percentEncode(oauthSecret)}"

        // Generate HMAC-SHA1 signature
        val mac = HmacSHA1(signingKey.encodeToByteArray())
        val rawSignature = mac.doFinal(signatureBase.encodeToByteArray())
        val signature = Base64.encode(rawSignature)

        // Build Authorization header
        val authParams = oauthParams + ("oauth_signature" to signature)
        val authHeader = authParams.entries.joinToString(", ") { (key, value) ->
            "${percentEncode(key)}=\"${percentEncode(value)}\""
        }

        return "OAuth $authHeader"
    }
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
@Suppress("DEPRECATION")
internal fun currentTimestamp(): String {
    return kotlinx.datetime.Clock.System.now().epochSeconds.toString()
}
