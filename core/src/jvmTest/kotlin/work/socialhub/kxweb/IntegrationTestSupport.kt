package work.socialhub.kxweb

import org.junit.jupiter.api.Assumptions.assumeTrue

internal fun authenticatedIntegrationXWeb(): XWeb {
    val enabled = System.getenv("XWEB_RUN_INTEGRATION_TESTS") == "true"
    val cookie = System.getenv("XWEB_COOKIE")
    val authToken = System.getenv("XWEB_AUTH_TOKEN")
    val csrfToken = System.getenv("XWEB_CSRF_TOKEN")
    val hasCredentials = !cookie.isNullOrBlank() ||
            (!authToken.isNullOrBlank() && !csrfToken.isNullOrBlank())
    assumeTrue(
        enabled && hasCredentials,
        "Set XWEB_RUN_INTEGRATION_TESTS=true and XWEB_COOKIE or auth token/CSRF token",
    )

    val config = XWebConfig().also {
        if (!cookie.isNullOrBlank()) {
            it.cookieString = cookie
            it.authToken = extractCookieValue(cookie, "auth_token")
            it.csrfToken = extractCookieValue(cookie, "ct0")
        } else {
            it.authToken = authToken
            it.csrfToken = csrfToken
        }
        it.clientTransactionId = System.getenv("XWEB_CLIENT_TRANSACTION_ID")
    }
    return XWebFactory.instance(config)
}

private fun extractCookieValue(cookie: String, name: String): String? {
    return cookie.split(";")
        .asSequence()
        .map { it.trim() }
        .firstOrNull { it.startsWith("$name=") }
        ?.substringAfter("=")
}
