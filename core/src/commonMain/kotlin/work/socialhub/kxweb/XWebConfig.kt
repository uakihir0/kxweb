package work.socialhub.kxweb

import work.socialhub.kxweb.domain.Service
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.js.JsExport

@OptIn(ExperimentalAtomicApi::class)
internal class ClientTransactionIdState {
    private class Slot(val value: String?)

    private val slot = AtomicReference(Slot(null))

    var value: String?
        get() = slot.load().value
        set(value) {
            slot.store(Slot(value))
        }

    fun consume(): String? {
        while (true) {
            val current = slot.load()
            val value = current.value?.takeIf { it.isNotBlank() } ?: return null
            if (slot.compareAndSet(current, Slot(null))) {
                return value
            }
        }
    }
}

internal data class XWebRequestContext(
    val config: XWebConfig,
    val pool: XWebSessionPool?,
    val session: XWebSession?,
)

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

    /**
     * Explicit x-client-transaction-id header value.
     * This is consumed by the next eligible request and then cleared.
     */
    var clientTransactionId: String?
        get() = clientTransactionIdState.value
        set(value) {
            clientTransactionIdState.value = value
        }

    @JsExport.Ignore
    internal var clientTransactionIdState = ClientTransactionIdState()

    /**
     * Supplies an x-client-transaction-id for each request.
     * The arguments are the HTTP method and URL path.
     */
    @JsExport.Ignore
    var clientTransactionIdProvider: ((String, String) -> String)? = null

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
        applySession(session)
        return this
    }

    internal fun consumeClientTransactionId(): String? {
        return clientTransactionIdState.consume()
    }

    internal fun resolveRequestContext(endpoint: String): XWebRequestContext {
        val pool = sessionPool
        val session = pool?.acquireSession(endpoint)
        val snapshot = copyForRequest(session)
        return XWebRequestContext(snapshot, pool, session)
    }

    private fun copyForRequest(session: XWebSession?): XWebConfig {
        return XWebConfig().also { snapshot ->
            snapshot.apiBaseUri = apiBaseUri
            snapshot.authToken = authToken
            snapshot.csrfToken = csrfToken
            snapshot.oauthToken = oauthToken
            snapshot.oauthSecret = oauthSecret
            snapshot.guestMode = guestMode
            snapshot.guestToken = guestToken
            snapshot.skipSSLValidation = skipSSLValidation
            snapshot.requestTimeoutMillis = requestTimeoutMillis
            snapshot.connectTimeoutMillis = connectTimeoutMillis
            snapshot.socketTimeoutMillis = socketTimeoutMillis
            snapshot.cookieString = cookieString
            snapshot.enableClientTransaction = enableClientTransaction
            snapshot.clientTransactionIdState = clientTransactionIdState
            snapshot.clientTransactionIdProvider = clientTransactionIdProvider
            snapshot.currentSession = session
            if (session != null) {
                snapshot.applySession(session)
            }
        }
    }

    private fun applySession(session: XWebSession) {
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
    }
}
