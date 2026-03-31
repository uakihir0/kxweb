package work.socialhub.kxweb

import work.socialhub.kxweb.internal.XWebImpl
import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
object XWebFactory {

    @JsName("instanceFromConfig")
    fun instance(config: XWebConfig = XWebConfig()): XWeb {
        return XWebImpl(config)
    }

    /**
     * Create an XWeb instance with cookie-based authentication.
     * Obtain authToken and csrfToken from browser DevTools:
     *   Application > Cookies > x.com > auth_token, ct0
     */
    @JsName("instanceFromCookies")
    fun instance(authToken: String, csrfToken: String): XWeb {
        return XWebImpl(
            XWebConfig().also {
                it.authToken = authToken
                it.csrfToken = csrfToken
            }
        )
    }

    /**
     * Create an XWeb instance with a full browser cookie string.
     * Parses auth_token and ct0 from the cookie string automatically.
     */
    @JsName("instanceFromCookieString")
    fun instanceFromCookieString(cookieString: String): XWeb {
        val authToken = extractCookieValue(cookieString, "auth_token")
        val ct0 = extractCookieValue(cookieString, "ct0")
        return XWebImpl(
            XWebConfig().also {
                it.cookieString = cookieString
                it.authToken = authToken
                it.csrfToken = ct0
            }
        )
    }

    /**
     * Create an XWeb instance with OAuth1 authentication.
     * Uses api.x.com endpoint with HMAC-SHA1 signature (same as Nitter).
     */
    @JsName("instanceFromOAuth")
    fun instanceOAuth(oauthToken: String, oauthSecret: String): XWeb {
        return XWebImpl(
            XWebConfig().also {
                it.oauthToken = oauthToken
                it.oauthSecret = oauthSecret
            }
        )
    }

    /**
     * Create an XWeb instance backed by a session pool.
     * The pool manages multiple sessions and automatically selects
     * the best available session for each request, handling rate limits.
     *
     * Reference: Nitter's session pool pattern (auth.nim)
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
    @JsName("instanceFromPool")
    fun instancePooled(pool: XWebSessionPool): XWeb {
        return XWebImpl(
            XWebConfig().also {
                it.sessionPool = pool
            }
        )
    }

    /**
     * Create an XWeb instance backed by a session pool with custom config.
     * Allows setting timeouts, SSL options, and other config alongside the pool.
     */
    @JsName("instanceFromPoolWithConfig")
    fun instancePooled(pool: XWebSessionPool, config: XWebConfig): XWeb {
        config.sessionPool = pool
        return XWebImpl(config)
    }

    /**
     * Create an XWeb instance with a session pool from a list of sessions.
     * Convenience method that creates the pool automatically.
     */
    @JsName("instanceFromSessions")
    fun instancePooled(sessions: List<XWebSession>): XWeb {
        return instancePooled(XWebSessionPool(sessions))
    }

    /**
     * Create an XWeb instance with a session pool loaded from JSONL string.
     * Compatible with Nitter's sessions.jsonl format.
     *
     * Cookie format: {"kind":"cookie","auth_token":"...","ct0":"...","username":"..."}
     * OAuth format:  {"oauth_token":"...","oauth_token_secret":"..."}
     */
    @JsName("instanceFromJsonLines")
    fun instancePooledFromJsonLines(jsonLines: String): XWeb {
        val sessions = XWebSession.parseJsonLines(jsonLines)
        return instancePooled(sessions)
    }

    private fun extractCookieValue(cookieString: String, name: String): String? {
        return cookieString.split(";")
            .map { it.trim() }
            .find { it.startsWith("$name=") }
            ?.substringAfter("=")
    }
}
