package work.socialhub.kxweb.entity.share

import kotlin.js.JsExport
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Rate limit information from X (Twitter) API response headers.
 *
 * X returns rate limit info via:
 * - x-rate-limit-limit: Maximum requests per window
 * - x-rate-limit-remaining: Remaining requests in current window
 * - x-rate-limit-reset: Unix timestamp when the window resets
 *
 * Reference: Nitter's rate limit tracking (auth.nim)
 */
@JsExport
data class RateLimit(
    /** Maximum number of requests allowed per window. */
    val limit: Int,
    /** Number of requests remaining in the current window. */
    val remaining: Int,
    /** Unix timestamp (seconds) when the rate limit window resets. */
    val resetEpochSeconds: Long,
) {
    /**
     * Whether this rate limit is currently exhausted.
     * A threshold of 2 remaining requests is used to avoid edge cases.
     */
    fun isLimited(): Boolean {
        if (remaining > REMAINING_THRESHOLD) return false
        val now = Clock.System.now()
        return now < Instant.fromEpochSeconds(resetEpochSeconds)
    }

    /**
     * Seconds until the rate limit resets, or 0 if already reset.
     */
    fun secondsUntilReset(): Long {
        val now = Clock.System.now().epochSeconds
        return maxOf(0L, resetEpochSeconds - now)
    }

    companion object {
        /** Consider rate-limited when remaining drops to this threshold. */
        const val REMAINING_THRESHOLD = 2

        /**
         * Parse rate limit from HTTP response headers.
         * Returns null if the required headers are not present.
         */
        fun fromHeaders(headers: Map<String, List<String>>): RateLimit? {
            val limit = headers.firstValue("x-rate-limit-limit")
                ?.toIntOrNull() ?: return null
            val remaining = headers.firstValue("x-rate-limit-remaining")
                ?.toIntOrNull() ?: return null
            val reset = headers.firstValue("x-rate-limit-reset")
                ?.toLongOrNull() ?: return null

            return RateLimit(
                limit = limit,
                remaining = remaining,
                resetEpochSeconds = reset,
            )
        }

        private fun Map<String, List<String>>.firstValue(key: String): String? {
            // Headers may be case-insensitive, try exact and lowercase
            return this[key]?.firstOrNull()
                ?: this[key.lowercase()]?.firstOrNull()
        }
    }
}
