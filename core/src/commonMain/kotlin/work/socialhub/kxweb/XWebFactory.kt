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

    private fun extractCookieValue(cookieString: String, name: String): String? {
        return cookieString.split(";")
            .map { it.trim() }
            .find { it.startsWith("$name=") }
            ?.substringAfter("=")
    }
}
