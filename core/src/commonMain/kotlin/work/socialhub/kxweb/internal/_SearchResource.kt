package work.socialhub.kxweb.internal

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import work.socialhub.kxweb.XWebConfig
import work.socialhub.kxweb.api.SearchResource
import work.socialhub.kxweb.domain.QueryId
import work.socialhub.kxweb.entity.search.SearchSearchRequest
import work.socialhub.kxweb.entity.search.SearchSearchResponse
import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.internal.entity.GraphQLSearchRoot
import work.socialhub.kxweb.internal.share._InternalUtility.fromJson
import work.socialhub.kxweb.internal.share._InternalUtility.graphqlUrl
import work.socialhub.kxweb.internal.share._InternalUtility.httpRequest
import work.socialhub.kxweb.internal.share._InternalUtility.isOAuth
import work.socialhub.kxweb.internal.share._InternalUtility.searchFeatures
import work.socialhub.kxweb.internal.share._InternalUtility.setTimeouts
import work.socialhub.kxweb.internal.share._InternalUtility.withCookieHeaders
import work.socialhub.kxweb.internal.share._InternalUtility.withOAuthHeaders
import work.socialhub.kxweb.model.Media
import work.socialhub.kxweb.model.Tweet
import work.socialhub.kxweb.model.User
import work.socialhub.kxweb.util.toBlocking

class _SearchResource(
    private val config: XWebConfig
) : SearchResource {

    override suspend fun searchTweets(
        request: SearchSearchRequest
    ): Response<SearchSearchResponse> {

        val url = graphqlUrl(config, QueryId.SEARCH_TIMELINE, "SearchTimeline")

        // Build variables as JsonObject
        val variables = buildJsonObject {
            request.query?.let { put("rawQuery", it) }
            put("count", request.count)
            put("querySource", "typed_query")
            put("product", request.searchType.product)
            request.cursor?.let { put("cursor", it) }
        }

        // Build features as JsonObject
        val features = buildJsonObject {
            searchFeatures().forEach { (key, value) ->
                put(key, value)
            }
        }

        val variablesStr = variables.toString()
        val featuresStr = features.toString()
        val queryParams = mapOf("variables" to variablesStr, "features" to featuresStr)

        val request2 = httpRequest(config)
            .url(url)
            .setTimeouts(config)
            .query("variables", variablesStr)
            .query("features", featuresStr)

        // Apply authentication headers based on config
        if (isOAuth(config)) {
            // OAuth1: uses api.x.com endpoint with HMAC-SHA1 signature
            request2.withOAuthHeaders(config, "GET", url, queryParams)
        } else {
            // Cookie: uses x.com/i/api endpoint with Bearer + cookies
            request2.withCookieHeaders(config)
        }

        val response = request2.get()

        val responseBody = response.stringBody

        if (response.status !in 200..299) {
            throw _InternalUtility.handleError(
                exception = null,
                status = response.status,
                body = responseBody,
            )
        }

        // Parse the deeply nested GraphQL response
        val graphQLResponse = fromJson<GraphQLSearchRoot>(responseBody)
        val searchResponse = parseSearchResponse(graphQLResponse)

        return Response(searchResponse, responseBody)
    }

    override fun searchTweetsBlocking(
        request: SearchSearchRequest
    ): Response<SearchSearchResponse> {
        return toBlocking {
            searchTweets(request)
        }
    }

    private fun parseSearchResponse(root: GraphQLSearchRoot): SearchSearchResponse {
        val instructions = root.data
            ?.searchByRawQuery
            ?.searchTimeline
            ?.timeline
            ?.instructions
            ?: return SearchSearchResponse()

        val tweets = mutableListOf<Tweet>()
        var cursor: String? = null

        for (instruction in instructions) {
            val entries = instruction.entries ?: continue

            for (entry in entries) {
                val content = entry.content ?: continue

                // Extract cursor for pagination
                if (content.cursorType == "Bottom") {
                    cursor = content.value
                    continue
                }

                // Extract tweet from entry
                val tweetResult = content.itemContent
                    ?.tweetResults
                    ?.result
                    ?: continue

                val legacy = tweetResult.legacy ?: continue

                // Build User model
                val userResult = tweetResult.core?.userResults?.result
                val userLegacy = userResult?.legacy
                val userCore = userResult?.core
                val user = if (userResult != null && (userCore != null || userLegacy != null)) {
                    User(
                        id = userResult.restId,
                        screenName = userCore?.screenName ?: userLegacy?.screenName,
                        name = userCore?.name ?: userLegacy?.name,
                        description = userLegacy?.description,
                        profileImageUrl = userLegacy?.profileImageUrlHttps,
                        followersCount = userLegacy?.followersCount,
                        followingCount = userLegacy?.friendsCount,
                        verified = userResult.isBlueVerified ?: userLegacy?.verified,
                    )
                } else null

                // Build Media models
                val mediaEntities = legacy.extendedEntities?.media
                    ?: legacy.entities?.media
                    ?: emptyList()

                val mediaList = mediaEntities.map { entity ->
                    Media(
                        type = entity.type,
                        url = entity.mediaUrlHttps,
                        width = entity.originalInfo?.width,
                        height = entity.originalInfo?.height,
                    )
                }

                // Build Tweet model
                tweets.add(
                    Tweet(
                        id = tweetResult.restId,
                        text = legacy.fullText,
                        createdAt = legacy.createdAt,
                        user = user,
                        replyCount = legacy.replyCount,
                        retweetCount = legacy.retweetCount,
                        favoriteCount = legacy.favoriteCount,
                        media = mediaList,
                        inReplyToStatusId = legacy.inReplyToStatusIdStr,
                        conversationId = legacy.conversationIdStr,
                        lang = legacy.lang,
                    )
                )
            }
        }

        return SearchSearchResponse(
            tweets = tweets,
            cursor = cursor,
        )
    }

    private object _InternalUtility {
        fun handleError(
            exception: Exception?,
            status: Int? = null,
            body: String? = null,
        ) = work.socialhub.kxweb.internal.share._InternalUtility.handleError(
            exception, status, body,
        )
    }
}
