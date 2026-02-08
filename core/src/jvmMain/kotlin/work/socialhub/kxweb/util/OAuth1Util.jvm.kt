package work.socialhub.kxweb.util

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

actual object OAuth1Util {

    actual fun generateAuthHeader(
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

        // All parameters (query + oauth) sorted
        val allParams = (queryParams + oauthParams).toSortedMap()

        // Build parameter string
        val paramString = allParams.entries.joinToString("&") { (key, value) ->
            "${percentEncode(key)}=${percentEncode(value)}"
        }

        // Build signature base string
        val signatureBase = "${method.uppercase()}&${percentEncode(url)}&${percentEncode(paramString)}"

        // Build signing key
        val signingKey = "${percentEncode(consumerSecret)}&${percentEncode(oauthSecret)}"

        // Generate HMAC-SHA1 signature
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(signingKey.encodeToByteArray(), "HmacSHA1"))
        val rawSignature = mac.doFinal(signatureBase.encodeToByteArray())
        val signature = java.util.Base64.getEncoder().encodeToString(rawSignature)

        // Build Authorization header
        val authParams = oauthParams + ("oauth_signature" to signature)
        val authHeader = authParams.entries.joinToString(", ") { (key, value) ->
            "${percentEncode(key)}=\"${percentEncode(value)}\""
        }

        return "OAuth $authHeader"
    }
}
