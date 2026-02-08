package work.socialhub.kxweb.util

actual object OAuth1Util {

    actual fun generateAuthHeader(
        method: String,
        url: String,
        queryParams: Map<String, String>,
        consumerKey: String,
        consumerSecret: String,
        oauthToken: String,
        oauthSecret: String,
    ): String {
        throw UnsupportedOperationException(
            "OAuth1 authentication is not yet supported on Native. " +
                    "Please use cookie-based authentication instead."
        )
    }
}
