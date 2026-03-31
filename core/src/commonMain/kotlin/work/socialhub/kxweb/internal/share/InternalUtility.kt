package work.socialhub.kxweb.internal.share

import kotlinx.serialization.json.Json
import work.socialhub.kxweb.XWebConfig
import work.socialhub.kxweb.XWebException
import work.socialhub.kxweb.domain.Service
import work.socialhub.kxweb.entity.share.RateLimit
import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.util.OAuth1Util
import work.socialhub.khttpclient.HttpRequest
import work.socialhub.khttpclient.HttpResponse

object InternalUtility {

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
     * Track rate limit information from an HTTP response.
     * When a session pool is configured, updates the pool with rate limit data
     * and handles session-level errors (rate limit exceeded, token invalidation).
     *
     * Should be called after every API response in ResourceImpl classes.
     *
     * @param config The current XWebConfig (may contain session pool).
     * @param endpoint The API endpoint name (e.g., "SearchTimeline").
     * @param response The HTTP response to extract rate limit headers from.
     */
    fun trackResponse(config: XWebConfig, endpoint: String, response: HttpResponse) {
        val pool = config.sessionPool ?: return
        val session = config.currentSession ?: return

        // Parse and update rate limit from headers
        val rateLimit = RateLimit.fromHeaders(response.headers)
        if (rateLimit != null) {
            pool.updateRateLimit(session, endpoint, rateLimit)
        }

        // Handle rate limit exceeded (HTTP 429)
        if (response.status == 429) {
            pool.markGloballyLimited(session)
        }

        // Handle invalid session errors
        // X error codes: 89 (invalid/expired token), 239 (bad auth data), 326 (account locked)
        if (response.status == 401 || response.status == 403) {
            try {
                val body = response.stringBody
                if (body.contains("\"code\":89") ||
                    body.contains("\"code\":239") ||
                    body.contains("\"code\":326")
                ) {
                    pool.invalidateSession(session)
                }
            } catch (_: Exception) {
                // Ignore parse errors
            }
        }
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

        val csrfToken = config.csrfToken
        if (csrfToken != null) {
            it.header("x-csrf-token", csrfToken)
            it.header("x-twitter-auth-type", "OAuth2Session")
        }

        val cookieString = config.cookieString
        if (cookieString != null) {
            it.header("cookie", cookieString)
        } else {
            val authToken = config.authToken
            if (authToken != null && csrfToken != null) {
                it.header("cookie", "auth_token=$authToken; ct0=$csrfToken")
            }
        }

        if (config.enableClientTransaction) {
            it.header("x-client-transaction-id", generateClientTransactionId())
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

    /**
     * Feature flags for HomeTimeline / HomeLatestTimeline.
     */
    fun homeTimelineFeatures(): Map<String, Boolean> = mapOf(
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

    /**
     * Feature flags for TweetDetail (conversation thread).
     */
    fun tweetDetailFeatures(): Map<String, Boolean> = mapOf(
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

    /**
     * Feature flags for UserByScreenName.
     */
    fun userByScreenNameFeatures(): Map<String, Boolean> = mapOf(
        "hidden_profile_subscriptions_enabled" to true,
        "rweb_tipjar_consumption_enabled" to true,
        "responsive_web_graphql_exclude_directive_enabled" to true,
        "verified_phone_label_enabled" to false,
        "subscriptions_verification_info_is_identity_verified_enabled" to true,
        "subscriptions_verification_info_verified_since_enabled" to true,
        "highlights_tweets_tab_ui_enabled" to true,
        "responsive_web_twitter_article_notes_tab_enabled" to true,
        "subscriptions_feature_can_gift_premium" to true,
        "creator_subscriptions_tweet_preview_api_enabled" to true,
        "responsive_web_graphql_skip_user_profile_image_extensions_enabled" to false,
        "responsive_web_graphql_timeline_navigation_enabled" to true,
    )

    /**
     * Feature flags for UserTweets.
     */
    fun userTweetsFeatures(): Map<String, Boolean> = mapOf(
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

    /**
     * Feature flags for Following / Followers.
     */
    fun followingFeatures(): Map<String, Boolean> = mapOf(
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

    /**
     * Feature flags for Lists (ownerships, memberships, timeline).
     */
    fun listsFeatures(): Map<String, Boolean> = mapOf(
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

    /**
     * Feature flags for Bookmarks.
     */
    fun bookmarksFeatures(): Map<String, Boolean> = mapOf(
        "graphql_timeline_v2_bookmark_timeline" to true,
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

    /**
     * Feature flags for Likes timeline.
     */
    fun likesFeatures(): Map<String, Boolean> = mapOf(
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

    /**
     * Feature flags for Explore / News (GenericTimelineById).
     */
    fun exploreFeatures(): Map<String, Boolean> = mapOf(
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

    /**
     * Feature flags for CreateTweet mutation.
     */
    fun tweetCreateFeatures(): Map<String, Boolean> = mapOf(
        "communities_web_enable_tweet_community_results_fetch" to true,
        "c9s_tweet_anatomy_moderator_badge_enabled" to true,
        "responsive_web_edit_tweet_api_enabled" to true,
        "graphql_is_translatable_rweb_tweet_is_translatable_enabled" to true,
        "view_counts_everywhere_api_enabled" to true,
        "longform_notetweets_consumption_enabled" to true,
        "responsive_web_twitter_article_tweet_consumption_enabled" to true,
        "tweet_awards_web_tipping_enabled" to false,
        "creator_subscriptions_quote_tweet_preview_enabled" to false,
        "longform_notetweets_rich_text_read_enabled" to true,
        "longform_notetweets_inline_media_enabled" to true,
        "articles_preview_enabled" to true,
        "rweb_video_timestamps_enabled" to true,
        "tweet_with_visibility_results_prefer_gql_limited_actions_policy_enabled" to true,
        "responsive_web_graphql_exclude_directive_enabled" to true,
        "verified_phone_label_enabled" to false,
        "freedom_of_speech_not_reach_fetch_enabled" to true,
        "standardized_nudges_misinfo" to true,
        "responsive_web_graphql_skip_user_profile_image_extensions_enabled" to false,
        "responsive_web_graphql_timeline_navigation_enabled" to true,
        "responsive_web_enhance_cards_enabled" to false,
    )

    /**
     * Apply authenticated headers based on config type (Cookie or OAuth).
     * When a session pool is configured, resolves the session from the pool first.
     * For GET requests, pass empty queryParams.
     *
     * @param endpoint Optional endpoint name for session pool rate limit selection.
     */
    fun HttpRequest.withAuthHeaders(
        config: XWebConfig,
        method: String,
        url: String,
        queryParams: Map<String, String> = emptyMap(),
        endpoint: String = "",
    ): HttpRequest {
        // Resolve session from pool if configured
        config.resolveSession(endpoint)

        return if (isOAuth(config)) {
            withOAuthHeaders(config, method, url, queryParams)
        } else {
            withCookieHeaders(config)
        }
    }

    /**
     * Build a GraphQL POST body for mutation endpoints.
     */
    fun graphqlPostBody(
        variables: String,
        queryId: String,
    ): String {
        return """{"variables":$variables,"queryId":"$queryId"}"""
    }

    /**
     * Build a GraphQL POST body with features for mutation endpoints.
     */
    fun graphqlPostBodyWithFeatures(
        variables: String,
        features: Map<String, Boolean>,
        queryId: String,
    ): String {
        val featuresJson = features.entries.joinToString(",") { (k, v) -> "\"$k\":$v" }
        return """{"variables":$variables,"features":{$featuresJson},"queryId":"$queryId"}"""
    }

    /**
     * Execute a GraphQL operation with automatic QueryId retry on 404.
     * If the initial call returns 404, resolves a fresh QueryId from JS bundles and retries.
     */
    suspend fun <T> withQueryIdRetry(
        operationName: String,
        queryId: String,
        execute: suspend (resolvedQueryId: String) -> T,
    ): T {
        return try {
            execute(queryId)
        } catch (e: XWebException) {
            if (e.status == 404) {
                val newQueryId = QueryIdResolver.resolve(operationName, queryId)
                if (newQueryId != queryId) {
                    execute(newQueryId)
                } else {
                    throw e
                }
            } else {
                throw e
            }
        }
    }

    /**
     * Generate a simple client transaction ID.
     */
    fun generateClientTransactionId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..20).map { chars.random() }.joinToString("")
    }

    /**
     * Apply client transaction header if enabled.
     */
    fun HttpRequest.withClientTransaction(config: XWebConfig): HttpRequest = also {
        if (config.enableClientTransaction) {
            it.header("x-client-transaction-id", generateClientTransactionId())
        }
    }

    /**
     * Build a REST API URL.
     */
    fun restApiUrl(path: String): String {
        return "${Service.X_REST_API.uri}$path"
    }
}
