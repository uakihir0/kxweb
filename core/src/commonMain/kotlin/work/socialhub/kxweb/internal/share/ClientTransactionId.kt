package work.socialhub.kxweb.internal.share

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import work.socialhub.kxweb.util.Sha256Util
import work.socialhub.khttpclient.HttpRequest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random
import kotlin.time.Clock

/**
 * Generates x-client-transaction-id headers for X (Twitter) API requests.
 * This is required for cookie-based sessions to pass bot detection.
 *
 * Reference: Nitter's tid.nim implementation.
 *
 * The algorithm:
 * 1. Fetch "pair" data from external repository (animationKey + verification)
 * 2. Compute SHA-256 hash of: method + "!" + path + "!" + timestamp + secret + animationKey
 * 3. Combine verification bytes, timestamp bytes, hash bytes, and XOR mask
 * 4. Base64-encode the result
 *
 * Pair data source: https://github.com/fa0311/x-client-transaction-id-pair-dict
 */
object ClientTransactionId {

    private const val PAIR_URL =
        "https://raw.githubusercontent.com/fa0311/x-client-transaction-id-pair-dict/refs/heads/main/pair.json"
    private const val SECRET = "obfiowerehiring"
    private const val CACHE_DURATION_SECONDS = 3600L

    private var cachedPair: TransactionPair? = null
    private var cacheTimestamp: Long = 0L

    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    internal data class PairResponse(
        val key: String? = null,
        val key_bytes: List<Int>? = null,
        val animation_key: String? = null,
    )

    internal data class TransactionPair(
        val keyBytes: ByteArray,
        val animationKey: String,
    )

    /**
     * Generate a client transaction ID for the given request path.
     *
     * @param method HTTP method (GET, POST, etc.)
     * @param path The URL path (e.g., "/i/api/graphql/abc/SearchTimeline")
     * @return Base64-encoded transaction ID, or a random fallback if pair data is unavailable.
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun generate(method: String = "GET", path: String = ""): String {
        val pair = cachedPair ?: return generateSimple()

        try {
            val now = Clock.System.now().epochSeconds
            val timeBytes = encodeTimestamp(now)

            // Hash: method + "!" + path + "!" + timestamp + secret + animationKey
            val hashInput = "$method!$path!$now$SECRET${pair.animationKey}"
            val hashBytes = Sha256Util.hash(hashInput.encodeToByteArray())

            // Take first 16 bytes of hash
            val hashSlice = hashBytes.copyOfRange(0, minOf(16, hashBytes.size))

            // Generate random XOR mask (1 byte)
            val xorMask = Random.nextBytes(1)[0]

            // Build payload: [verification_key_bytes] + [time_bytes] + [hash_bytes]
            val payload = pair.keyBytes + timeBytes + hashSlice

            // XOR the payload with the mask
            val xored = ByteArray(payload.size) { i ->
                (payload[i].toInt() xor xorMask.toInt()).toByte()
            }

            // Final: [xor_mask] + [xored_data]
            val result = byteArrayOf(xorMask) + xored

            return Base64.encode(result)
        } catch (_: Exception) {
            return generateSimple()
        }
    }

    /**
     * Refresh the pair data from the external repository.
     * Should be called periodically (default: every hour).
     */
    suspend fun refreshPairData() {
        val now = Clock.System.now().epochSeconds
        if (cachedPair != null && (now - cacheTimestamp) < CACHE_DURATION_SECONDS) {
            return
        }

        try {
            val response = HttpRequest()
                .url(PAIR_URL)
                .get()

            if (response.status in 200..299) {
                val body = response.stringBody
                val pairResponse = json.decodeFromString<PairResponse>(body)
                val keyBytes = pairResponse.key_bytes?.map { it.toByte() }?.toByteArray()
                val animationKey = pairResponse.animation_key

                if (keyBytes != null && animationKey != null) {
                    cachedPair = TransactionPair(keyBytes, animationKey)
                    cacheTimestamp = now
                }
            }
        } catch (_: Exception) {
            // Pair data fetch failed; will use simple fallback
        }
    }

    /**
     * Check if pair data is cached and available.
     */
    fun isPairDataAvailable(): Boolean = cachedPair != null

    /**
     * Clear cached pair data (for testing).
     */
    internal fun clearCache() {
        cachedPair = null
        cacheTimestamp = 0L
    }

    /**
     * Set pair data directly (for testing).
     */
    internal fun setPairData(keyBytes: ByteArray, animationKey: String) {
        cachedPair = TransactionPair(keyBytes, animationKey)
        cacheTimestamp = Clock.System.now().epochSeconds
    }

    /**
     * Simple random fallback transaction ID (original implementation).
     */
    private fun generateSimple(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..20).map { chars.random() }.joinToString("")
    }

    /**
     * Encode a Unix timestamp into a compact byte representation.
     */
    private fun encodeTimestamp(epochSeconds: Long): ByteArray {
        return byteArrayOf(
            ((epochSeconds shr 24) and 0xFF).toByte(),
            ((epochSeconds shr 16) and 0xFF).toByte(),
            ((epochSeconds shr 8) and 0xFF).toByte(),
            (epochSeconds and 0xFF).toByte(),
        )
    }
}
