package work.socialhub.kxweb

import work.socialhub.kxweb.internal._XWeb
import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
object XWebFactory {

    @JsName("instanceFromConfig")
    fun instance(config: XWebConfig = XWebConfig()): XWeb {
        return _XWeb(config)
    }

    /**
     * Create an XWeb instance with cookie-based authentication.
     * Obtain authToken and csrfToken from browser DevTools:
     *   Application > Cookies > x.com > auth_token, ct0
     */
    @JsName("instanceFromCookies")
    fun instance(authToken: String, csrfToken: String): XWeb {
        return _XWeb(
            XWebConfig().also {
                it.authToken = authToken
                it.csrfToken = csrfToken
            }
        )
    }

    /**
     * Create an XWeb instance with OAuth1 authentication.
     * Uses api.x.com endpoint with HMAC-SHA1 signature (same as Nitter).
     */
    @JsName("instanceFromOAuth")
    fun instanceOAuth(oauthToken: String, oauthSecret: String): XWeb {
        return _XWeb(
            XWebConfig().also {
                it.oauthToken = oauthToken
                it.oauthSecret = oauthSecret
            }
        )
    }
}
