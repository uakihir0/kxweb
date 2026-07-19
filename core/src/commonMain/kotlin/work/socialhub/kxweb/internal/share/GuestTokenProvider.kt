package work.socialhub.kxweb.internal.share

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import work.socialhub.kxweb.XWebConfig
import work.socialhub.kxweb.domain.Service
import work.socialhub.kxweb.internal.share.InternalUtility.fromJson
import work.socialhub.kxweb.internal.share.InternalUtility.httpRequest
import work.socialhub.kxweb.internal.share.InternalUtility.setTimeouts
import work.socialhub.kxweb.internal.share.InternalUtility.withBearerHeaders
import kotlin.concurrent.Volatile
import kotlin.time.Clock

/**
 * Acquires and caches guest tokens for X (Twitter) web API.
 *
 * A guest token allows read-only access to a limited set of endpoints
 * (e.g. UserByScreenName, TweetResultByRestId, UserTweets, trends) without
 * a logged-in account. Guest tokens expire after a few hours, so the token
 * is cached with a TTL and refreshed on demand.
 *
 * Reference: Nitter's guest account activation and QuaX's guest token flow.
 *
 * Endpoint: POST https://api.x.com/1.1/guest/activate.json
 */
object GuestTokenProvider {

    @Volatile
    private var cachedToken: String? = null

    @Volatile
    private var acquiredAtSeconds: Long = 0L

    /** Serializes activation so concurrent callers don't each hit the network. */
    private val activationMutex = Mutex()

    /** Guest tokens live roughly a few hours; refresh conservatively. */
    private const val TTL_SECONDS = 3 * 60 * 60L

    @Serializable
    internal data class GuestActivateResponse(
        @SerialName("guest_token")
        val guestToken: String? = null,
    )

    /**
     * Return a valid guest token, activating a new one if the cache is empty
     * or expired. A token set manually via [XWebConfig.guestToken] takes
     * precedence and is used without a network call.
     *
     * Concurrent callers are serialized: the first activates and caches the
     * token, and the rest reuse the fresh cached value instead of each making
     * a redundant activation request.
     */
    suspend fun token(config: XWebConfig): String {
        config.guestToken?.let { return it }

        cachedIfFresh()?.let { return it }

        return activationMutex.withLock {
            // Re-check inside the lock: another caller may have just refreshed.
            cachedIfFresh() ?: activate(config)
        }
    }

    /** Return the cached token if present and still within the TTL. */
    private fun cachedIfFresh(): String? {
        val cached = cachedToken ?: return null
        val now = Clock.System.now().epochSeconds
        return if ((now - acquiredAtSeconds) < TTL_SECONDS) cached else null
    }

    /**
     * Activate a fresh guest token via the public activation endpoint and
     * cache it. Uses the public Bearer token only (no user credentials).
     *
     * Prefer [token] for normal use; it deduplicates concurrent activations.
     */
    suspend fun activate(config: XWebConfig): String {
        val url = "${Service.X_REST_API_PUBLIC.uri}/1.1/guest/activate.json"

        val response = httpRequest(config)
            .url(url)
            .setTimeouts(config)
            .withBearerHeaders()
            .post()

        if (response.status !in 200..299) {
            throw InternalUtility.handleError(null, response.status, response.stringBody)
        }

        val token = fromJson<GuestActivateResponse>(response.stringBody).guestToken
            ?: throw InternalUtility.handleError(null, body = "Failed to activate guest token")

        cachedToken = token
        acquiredAtSeconds = Clock.System.now().epochSeconds
        return token
    }

    /**
     * Clear the cached guest token, forcing re-activation on the next request.
     * Called when a request fails with an authentication error.
     */
    fun invalidate() {
        cachedToken = null
        acquiredAtSeconds = 0L
    }
}
