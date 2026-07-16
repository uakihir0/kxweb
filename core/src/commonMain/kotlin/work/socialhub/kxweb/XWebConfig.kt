package work.socialhub.kxweb

import work.socialhub.kxweb.domain.Service
import kotlin.js.JsExport

@JsExport
open class XWebConfig {

    /**
     * Base URI for X (Twitter) GraphQL API.
     */
    var apiBaseUri: String = Service.X_API_GRAPHQL.uri

    // == Cookie-based authentication ==
    // Obtain from browser DevTools > Application > Cookies > x.com
    // Endpoint: https://x.com/i/api/graphql

    /**
     * Cookie: auth_token value from X (Twitter) web browser session.
     */
    var authToken: String? = null

    /**
     * Cookie: ct0 value (CSRF token) from X (Twitter) web browser session.
     */
    var csrfToken: String? = null

    // == OAuth1 authentication ==
    // Uses api.x.com endpoint with OAuth1 signature (same as Nitter).
    // Endpoint: https://api.x.com/graphql

    /**
     * OAuth1 access token for X (Twitter) API.
     */
    var oauthToken: String? = null

    /**
     * OAuth1 access token secret for X (Twitter) API.
     */
    var oauthSecret: String? = null

    // == Guest authentication ==
    // Read-only access to a limited set of endpoints without a user account.
    // A guest token is acquired via POST /1.1/guest/activate.json and sent as
    // the x-guest-token header alongside the public Bearer token.
    // Endpoint: https://api.x.com/graphql

    /**
     * Force guest mode. When true, requests use guest-token authentication
     * even if no other credentials are set. Guest auth is also used
     * transparently when no cookie/OAuth credentials are present.
     */
    var guestMode: Boolean = false

    /**
     * Guest token value. When set, it is used directly instead of activating
     * a new one. Leave null to acquire and cache a token automatically.
     */
    var guestToken: String? = null

    /**
     * Skip SSL Validation (Kotlin/JVM Only)
     */
    var skipSSLValidation: Boolean = false

    /**
     * Specifies a request timeout in milliseconds.
     */
    var requestTimeoutMillis: Long? = null

    /**
     * Specifies a connection timeout in milliseconds.
     */
    var connectTimeoutMillis: Long? = null

    /**
     * Specifies a socket timeout (read and write) in milliseconds.
     */
    var socketTimeoutMillis: Long? = null

    /**
     * Full browser cookie string.
     * When set, this is sent as the Cookie header directly,
     * overriding the auth_token/ct0 individual fields for the cookie header.
     * auth_token and csrfToken are still used for x-csrf-token header.
     */
    var cookieString: String? = null

    /**
     * Enable x-client-transaction-id header generation.
     */
    var enableClientTransaction: Boolean = false

    // == Session Pool ==

    /**
     * Session pool for managing multiple authenticated sessions.
     * When set, credentials are resolved from the pool instead of
     * the static authToken/csrfToken/oauthToken/oauthSecret fields.
     *
     * @see XWebSessionPool
     */
    @JsExport.Ignore
    var sessionPool: XWebSessionPool? = null

    /**
     * The currently active session resolved from the pool.
     * Set internally before each request when using a session pool.
     * Do not set this manually.
     */
    @JsExport.Ignore
    var currentSession: XWebSession? = null

    /**
     * Resolve the effective authentication from the session pool (if set)
     * or from the static config fields.
     *
     * @param endpoint The API endpoint name for rate limit selection.
     * @return A resolved [XWebConfig] snapshot with credentials set.
     */
    fun resolveSession(endpoint: String = ""): XWebConfig {
        val pool = sessionPool ?: return this
        val session = pool.acquireSession(endpoint) ?: return this

        currentSession = session
        when (session) {
            is XWebSession.Cookie -> {
                authToken = session.authToken
                csrfToken = session.csrfToken
                cookieString = session.cookieString
                oauthToken = null
                oauthSecret = null
            }
            is XWebSession.OAuth -> {
                oauthToken = session.oauthToken
                oauthSecret = session.oauthSecret
                authToken = null
                csrfToken = null
                cookieString = null
            }
        }
        return this
    }
}
