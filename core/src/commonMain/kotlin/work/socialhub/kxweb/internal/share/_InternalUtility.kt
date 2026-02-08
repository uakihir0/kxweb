package work.socialhub.kxweb.internal.share

import kotlinx.serialization.json.Json
import work.socialhub.kxweb.XWebConfig
import work.socialhub.kxweb.XWebException
import work.socialhub.kxweb.domain.Service
import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.util.OAuth1Util
import work.socialhub.khttpclient.HttpRequest
import work.socialhub.khttpclient.HttpResponse

object _InternalUtility {

    /**
     * Public Bearer Token for X (Twitter) web API.
     * This is the same token used by the web client and is publicly known.
     * Used for cookie-based authentication.
     */
    const val BEARER_TOKEN =
        "AAAAAAAAAAAAAAAAAAAAANRILgAAAAAAnNwIzUejRCOuH5E6I8xnZz4puTs" +
                "%3D1Zv7ttfk8LF81IUq16cHjhLTvJu4FA33AGWWjCpTnA"

    /**
     * OAuth1 consumer key for X (Twitter) API.
     * This is a public consumer key used by various X clients.
     */
    const val CONSUMER_KEY = "3nVuSoBZnx6U4vzUxf5w"
    const val CONSUMER_SECRET = "Bcs59EFbbsdF6Sl9Ng71smgStWEGwXXKSjYvPVt7qys"

    const val USER_AGENT =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"

    val json = Json {
        explicitNulls = false
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    inline fun <reified T> toJson(obj: T): String {
        return json.encodeToString(obj)
    }

    inline fun <reified T> fromJson(obj: String): T {
        return json.decodeFromString(obj)
    }

    /**
     * Process HTTP response and return typed Response.
     */
    suspend inline fun <reified T> proceed(
        crossinline function: suspend () -> HttpResponse
    ): Response<T> {
        try {
            val response: HttpResponse = function()
            if (response.status in 200..299) {
                return Response(
                    response.typedBody(json),
                    response.stringBody,
                )
            }

            throw handleError(
                exception = null,
                status = response.status,
                body = response.stringBody,
            )
        } catch (e: Exception) {
            throw handleError(e)
        }
    }

    fun handleError(
        exception: Exception?,
        status: Int? = null,
        body: String? = null,
    ): RuntimeException {

        if (exception is XWebException) {
            return exception
        }

        return XWebException(
            message = body ?: exception?.message ?: "Unknown error",
            exception = exception,
            status = status,
            body = body,
        )
    }

    /**
     * Determine if the config uses OAuth1 authentication.
     */
    fun isOAuth(config: XWebConfig): Boolean {
        return config.oauthToken != null && config.oauthSecret != null
    }

    /**
     * Get the GraphQL base URI based on authentication method.
     * - OAuth: https://api.x.com/graphql
     * - Cookie: https://x.com/i/api/graphql (from config.apiBaseUri)
     */
    fun graphqlBaseUri(config: XWebConfig): String {
        return if (isOAuth(config)) {
            Service.X_API_GRAPHQL_OAUTH.uri
        } else {
            config.apiBaseUri
        }
    }

    fun graphqlUrl(config: XWebConfig, queryId: String, operationName: String): String {
        var base = graphqlBaseUri(config)
        if (!base.endsWith("/")) {
            base += "/"
        }
        return "$base$queryId/$operationName"
    }

    fun httpRequest(config: XWebConfig): HttpRequest {
        return HttpRequest().also {
            it.skipSSLValidation(config.skipSSLValidation)
        }
    }

    /**
     * Apply cookie-based authentication headers.
     * Uses public Bearer Token + auth_token/ct0 cookies.
     */
    fun HttpRequest.withCookieHeaders(config: XWebConfig): HttpRequest = also {
        it.header("authorization", "Bearer $BEARER_TOKEN")
        it.header("user-agent", USER_AGENT)
        it.header("x-twitter-active-user", "yes")
        it.header("x-twitter-client-language", "en")
        it.header("accept", "*/*")
        it.header("origin", "https://x.com")
        it.header("referer", "https://x.com/")

        val authToken = config.authToken
        val csrfToken = config.csrfToken
        if (authToken != null && csrfToken != null) {
            it.header("x-csrf-token", csrfToken)
            it.header("x-twitter-auth-type", "OAuth2Session")
            it.header("cookie", "auth_token=$authToken; ct0=$csrfToken")
        }
    }

    /**
     * Apply OAuth1 authentication headers.
     * Uses HMAC-SHA1 signature with consumer key/secret and oauth token/secret.
     */
    fun HttpRequest.withOAuthHeaders(
        config: XWebConfig,
        method: String,
        url: String,
        queryParams: Map<String, String>,
    ): HttpRequest = also {
        val oauthToken = config.oauthToken ?: error("oauthToken is required")
        val oauthSecret = config.oauthSecret ?: error("oauthSecret is required")

        val authHeader = OAuth1Util.generateAuthHeader(
            method = method,
            url = url,
            queryParams = queryParams,
            consumerKey = CONSUMER_KEY,
            consumerSecret = CONSUMER_SECRET,
            oauthToken = oauthToken,
            oauthSecret = oauthSecret,
        )

        it.header("authorization", authHeader)
        it.header("user-agent", USER_AGENT)
        it.header("x-twitter-active-user", "yes")
        it.header("x-twitter-client-language", "en")
        it.header("accept", "*/*")
    }

    fun HttpRequest.setTimeouts(config: XWebConfig) = also {
        this.requestTimeoutMillis = config.requestTimeoutMillis
        this.connectTimeoutMillis = config.connectTimeoutMillis
        this.socketTimeoutMillis = config.socketTimeoutMillis
    }

    /**
     * Apply Bearer-token-only authentication headers.
     * No user credentials required. Works for endpoints like TweetResultByRestId.
     */
    fun HttpRequest.withBearerHeaders(): HttpRequest = also {
        it.header("authorization", "Bearer $BEARER_TOKEN")
        it.header("user-agent", USER_AGENT)
        it.header("accept", "*/*")
    }

    /**
     * Build GraphQL URL using api.x.com (Bearer-only endpoints always use api.x.com).
     */
    fun graphqlUrlPublic(queryId: String, operationName: String): String {
        var base = Service.X_API_GRAPHQL_OAUTH.uri
        if (!base.endsWith("/")) {
            base += "/"
        }
        return "$base$queryId/$operationName"
    }

    /**
     * Feature flags required for TweetResultByRestId GraphQL requests.
     * Extracted from X web client request.
     */
    fun tweetFeatures(): Map<String, Boolean> = mapOf(
        "creator_subscriptions_tweet_preview_api_enabled" to true,
        "premium_content_api_read_enabled" to false,
        "communities_web_enable_tweet_community_results_fetch" to true,
        "c9s_tweet_anatomy_moderator_badge_enabled" to true,
        "responsive_web_grok_analyze_button_fetch_trends_enabled" to false,
        "responsive_web_grok_analyze_post_followups_enabled" to false,
        "responsive_web_jetfuel_frame" to true,
        "responsive_web_grok_share_attachment_enabled" to true,
        "responsive_web_grok_annotations_enabled" to true,
        "articles_preview_enabled" to true,
        "responsive_web_edit_tweet_api_enabled" to true,
        "graphql_is_translatable_rweb_tweet_is_translatable_enabled" to true,
        "view_counts_everywhere_api_enabled" to true,
        "longform_notetweets_consumption_enabled" to true,
        "responsive_web_twitter_article_tweet_consumption_enabled" to true,
        "tweet_awards_web_tipping_enabled" to false,
        "responsive_web_grok_show_grok_translated_post" to false,
        "responsive_web_grok_analysis_button_from_backend" to true,
        "post_ctas_fetch_enabled" to false,
        "creator_subscriptions_quote_tweet_preview_enabled" to false,
        "freedom_of_speech_not_reach_fetch_enabled" to true,
        "standardized_nudges_misinfo" to true,
        "tweet_with_visibility_results_prefer_gql_limited_actions_policy_enabled" to true,
        "longform_notetweets_rich_text_read_enabled" to true,
        "longform_notetweets_inline_media_enabled" to true,
        "profile_label_improvements_pcf_label_in_post_enabled" to true,
        "responsive_web_profile_redirect_enabled" to false,
        "rweb_tipjar_consumption_enabled" to false,
        "verified_phone_label_enabled" to false,
        "responsive_web_grok_image_annotation_enabled" to true,
        "responsive_web_grok_imagine_annotation_enabled" to true,
        "responsive_web_grok_community_note_auto_translation_is_enabled" to false,
        "responsive_web_graphql_skip_user_profile_image_extensions_enabled" to false,
        "responsive_web_graphql_timeline_navigation_enabled" to true,
        "responsive_web_enhance_cards_enabled" to false,
    )

    /**
     * Field toggles for TweetResultByRestId GraphQL requests.
     */
    fun tweetFieldToggles(): Map<String, Boolean> = mapOf(
        "withArticleRichContentState" to true,
        "withArticlePlainText" to false,
        "withGrokAnalyze" to false,
        "withDisallowedReplyControls" to false,
    )

    /**
     * Feature flags required for SearchTimeline GraphQL requests.
     * These flags are sent with the request to control response format.
     *
     * Reference: bird project twitter-client-features.ts, Nitter consts.nim
     */
    fun searchFeatures(): Map<String, Boolean> = mapOf(
        "rweb_tipjar_consumption_enabled" to true,
        "responsive_web_graphql_exclude_directive_enabled" to true,
        "verified_phone_label_enabled" to false,
        "creator_subscriptions_tweet_preview_api_enabled" to true,
        "responsive_web_graphql_timeline_navigation_enabled" to true,
        "responsive_web_graphql_skip_user_profile_image_extensions_enabled" to false,
        "communities_web_enable_tweet_community_results_fetch" to true,
        "c9s_tweet_anatomy_moderator_badge_enabled" to true,
        "articles_preview_enabled" to true,
        "responsive_web_edit_tweet_api_enabled" to true,
        "graphql_is_translatable_rweb_tweet_is_translatable_enabled" to true,
        "view_counts_everywhere_api_enabled" to true,
        "longform_notetweets_consumption_enabled" to true,
        "responsive_web_twitter_article_tweet_consumption_enabled" to true,
        "tweet_awards_web_tipping_enabled" to false,
        "creator_subscriptions_quote_tweet_preview_enabled" to false,
        "freedom_of_speech_not_reach_fetch_enabled" to true,
        "standardized_nudges_misinfo" to true,
        "tweet_with_visibility_results_prefer_gql_limited_actions_policy_enabled" to true,
        "rweb_video_timestamps_enabled" to true,
        "longform_notetweets_rich_text_read_enabled" to true,
        "longform_notetweets_inline_media_enabled" to true,
        "responsive_web_enhance_cards_enabled" to false,
    )
}
