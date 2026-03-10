package work.socialhub.kxweb.domain

enum class Service(
    val uri: String
) {
    /** Cookie-based authentication endpoint */
    X_API_GRAPHQL("https://x.com/i/api/graphql"),

    /** OAuth1 authentication endpoint (used by Nitter) */
    X_API_GRAPHQL_OAUTH("https://api.x.com/graphql"),

    /** REST API v1.1 (cookie-based) */
    X_REST_API("https://x.com/i/api"),

    /** REST API v1.1 (public / OAuth) */
    X_REST_API_PUBLIC("https://api.x.com"),

    /** Media upload endpoint */
    X_UPLOAD("https://upload.twitter.com/1.1/media/upload.json"),
}
