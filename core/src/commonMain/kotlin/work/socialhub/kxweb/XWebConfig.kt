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
}
