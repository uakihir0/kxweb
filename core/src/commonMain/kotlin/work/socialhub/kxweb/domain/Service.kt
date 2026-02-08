package work.socialhub.kxweb.domain

enum class Service(
    val uri: String
) {
    /** Cookie-based authentication endpoint */
    X_API_GRAPHQL("https://x.com/i/api/graphql"),

    /** OAuth1 authentication endpoint (used by Nitter) */
    X_API_GRAPHQL_OAUTH("https://api.x.com/graphql"),
}
