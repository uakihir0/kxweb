package work.socialhub.kxweb

import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
object KxwebFactory {

    @JsName("instanceFromConfig")
    fun instance(config: XWebConfig = XWebConfig()): XWeb {
        return XWebFactory.instance(config)
    }

    @JsName("instanceFromCookies")
    fun instance(authToken: String, csrfToken: String): XWeb {
        return XWebFactory.instance(authToken, csrfToken)
    }

    @JsName("instanceFromCookieString")
    fun instanceFromCookieString(cookieString: String): XWeb {
        return XWebFactory.instanceFromCookieString(cookieString)
    }

    @JsName("instanceFromOAuth")
    fun instanceOAuth(oauthToken: String, oauthSecret: String): XWeb {
        return XWebFactory.instanceOAuth(oauthToken, oauthSecret)
    }

    @JsName("instanceFromPool")
    fun instancePooled(pool: XWebSessionPool): XWeb {
        return XWebFactory.instancePooled(pool)
    }

    @JsName("instanceFromSessions")
    fun instancePooled(sessions: List<XWebSession>): XWeb {
        return XWebFactory.instancePooled(sessions)
    }

    @JsName("instanceFromJsonLines")
    fun instancePooledFromJsonLines(jsonLines: String): XWeb {
        return XWebFactory.instancePooledFromJsonLines(jsonLines)
    }
}
