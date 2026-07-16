package work.socialhub.kxweb

import kotlin.time.Clock
import kotlin.time.Instant
import work.socialhub.kxweb.entity.share.RateLimit
import kotlin.js.JsExport
import kotlin.time.Duration.Companion.seconds

/**
 * Manages a pool of [XWebSession]s with per-session, per-endpoint rate limit tracking.
 * Automatically selects the best available session for each request,
 * avoiding rate-limited or invalidated sessions.
 *
 * Reference: Nitter's session pool management (auth.nim)
 *
 * Usage:
 * ```kotlin
 * val pool = XWebSessionPool(
 *     listOf(
 *         XWebSession.cookie(authToken1, csrfToken1),
 *         XWebSession.cookie(authToken2, csrfToken2),
 *         XWebSession.oauth(oauthToken1, oauthSecret1),
 *     )
 * )
 * val xweb = XWebFactory.instancePooled(pool)
 * ```
 */
@JsExport
class XWebSessionPool(
    sessions: List<XWebSession>,
) {
    private val entries: MutableList<SessionEntry> =
        sessions.map { SessionEntry(it) }.toMutableList()

    /** Duration in seconds to mark a session as globally limited. */
    var globalLimitDurationSeconds: Long = 3600L

    /**
     * Internal state for a single session in the pool.
     */
    private class SessionEntry(
        val session: XWebSession,
    ) {
        /** Per-endpoint rate limit tracking. */
        val rateLimits: MutableMap<String, RateLimit> = mutableMapOf()
        /** Global limit flag (e.g., error 88 "Rate limit exceeded"). */
        var globalLimitUntil: Instant? = null
        /** Whether this session has been permanently invalidated. */
        var invalidated: Boolean = false
    }

    /**
     * Total number of sessions in the pool (including invalidated ones).
     */
    val size: Int get() = entries.size

    /**
     * Number of currently available (non-invalidated) sessions.
     */
    val availableCount: Int
        get() = entries.count { !it.invalidated }

    /**
     * Select the best available session for the given endpoint.
     * Returns null if all sessions are rate-limited or invalidated.
     *
     * Selection strategy:
     * 1. Skip invalidated sessions
     * 2. Skip globally limited sessions
     * 3. Skip sessions that are rate-limited for this endpoint
     * 4. Among remaining, prefer the session with the most remaining requests
     * 5. If all sessions are limited, return the one that resets soonest
     */
    fun acquireSession(endpoint: String = ""): XWebSession? {
        val now = Clock.System.now()

        // First pass: find sessions that are ready
        val ready = entries.filter { entry ->
            !entry.invalidated && !isGloballyLimited(entry, now) && !isEndpointLimited(entry, endpoint)
        }

        if (ready.isNotEmpty()) {
            // Pick session with most remaining requests for this endpoint
            return ready.maxByOrNull { entry ->
                entry.rateLimits[endpoint]?.remaining ?: Int.MAX_VALUE
            }?.session
        }

        // Second pass: all sessions are limited, pick the one that resets soonest
        val notInvalidated = entries.filter { !it.invalidated }
        if (notInvalidated.isEmpty()) return null

        return notInvalidated.minByOrNull { entry ->
            val globalReset = entry.globalLimitUntil?.epochSeconds ?: 0L
            val endpointReset = entry.rateLimits[endpoint]?.resetEpochSeconds ?: 0L
            maxOf(globalReset, endpointReset)
        }?.session
    }

    /**
     * Update rate limit information for a session after a response.
     */
    fun updateRateLimit(session: XWebSession, endpoint: String, rateLimit: RateLimit) {
        val entry = entries.find { it.session == session } ?: return
        entry.rateLimits[endpoint] = rateLimit
    }

    /**
     * Mark a session as globally rate-limited (e.g., X error code 88).
     */
    fun markGloballyLimited(session: XWebSession) {
        val entry = entries.find { it.session == session } ?: return
        entry.globalLimitUntil = Clock.System.now() + globalLimitDurationSeconds.seconds
    }

    /**
     * Mark a session as permanently invalidated (e.g., X error codes 89, 239, 326).
     * The session will no longer be selected.
     */
    fun invalidateSession(session: XWebSession) {
        val entry = entries.find { it.session == session } ?: return
        entry.invalidated = true
    }

    /**
     * Add a new session to the pool.
     */
    fun addSession(session: XWebSession) {
        entries.add(SessionEntry(session))
    }

    /**
     * Remove a session from the pool.
     */
    fun removeSession(session: XWebSession) {
        entries.removeAll { it.session == session }
    }

    /**
     * Get rate limit info for a session and endpoint.
     */
    fun getRateLimit(session: XWebSession, endpoint: String): RateLimit? {
        return entries.find { it.session == session }?.rateLimits?.get(endpoint)
    }

    /**
     * Check if a specific session is currently available for the given endpoint.
     */
    fun isSessionAvailable(session: XWebSession, endpoint: String = ""): Boolean {
        val entry = entries.find { it.session == session } ?: return false
        if (entry.invalidated) return false
        val now = Clock.System.now()
        if (isGloballyLimited(entry, now)) return false
        if (isEndpointLimited(entry, endpoint)) return false
        return true
    }

    private fun isGloballyLimited(entry: SessionEntry, now: Instant): Boolean {
        val until = entry.globalLimitUntil ?: return false
        return if (now < until) {
            true
        } else {
            entry.globalLimitUntil = null
            false
        }
    }

    private fun isEndpointLimited(entry: SessionEntry, endpoint: String): Boolean {
        val rateLimit = entry.rateLimits[endpoint] ?: return false
        return rateLimit.isLimited()
    }
}
