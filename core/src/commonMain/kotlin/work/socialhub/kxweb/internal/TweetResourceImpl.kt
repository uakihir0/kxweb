package work.socialhub.kxweb.internal

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import work.socialhub.kxweb.XWebConfig
import work.socialhub.kxweb.api.TweetResource
import work.socialhub.kxweb.domain.QueryId
import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.internal.entity.GraphQLTweetRoot
import work.socialhub.kxweb.internal.entity.TweetResult
import work.socialhub.kxweb.internal.share.InternalUtility.fromJson
import work.socialhub.kxweb.internal.share.InternalUtility.graphqlUrlPublic
import work.socialhub.kxweb.internal.share.InternalUtility.httpRequest
import work.socialhub.kxweb.internal.share.InternalUtility.setTimeouts
import work.socialhub.kxweb.internal.share.InternalUtility.tweetFeatures
import work.socialhub.kxweb.internal.share.InternalUtility.tweetFieldToggles
import work.socialhub.kxweb.internal.share.InternalUtility.withBearerHeaders
import work.socialhub.kxweb.model.Media
import work.socialhub.kxweb.model.Tweet
import work.socialhub.kxweb.model.User
import work.socialhub.kxweb.util.toBlocking

class TweetResourceImpl(
    private val config: XWebConfig
) : TweetResource {

    override suspend fun getTweet(
        tweetId: String
    ): Response<Tweet> {

        val url = graphqlUrlPublic(QueryId.TWEET_RESULT_BY_REST_ID, "TweetResultByRestId")

        val variables = buildJsonObject {
            put("tweetId", tweetId)
            put("withCommunity", false)
            put("includePromotedContent", false)
            put("withVoice", false)
        }

        val features = buildJsonObject {
            tweetFeatures().forEach { (key, value) ->
                put(key, value)
            }
        }

        val fieldToggles = buildJsonObject {
            tweetFieldToggles().forEach { (key, value) ->
                put(key, value)
            }
        }

        val request = httpRequest(config)
            .url(url)
            .setTimeouts(config)
            .query("variables", variables.toString())
            .query("features", features.toString())
            .query("fieldToggles", fieldToggles.toString())
            .withBearerHeaders()

        val response = request.get()
        val responseBody = response.stringBody

        if (response.status !in 200..299) {
            throw InternalUtility.handleError(
                exception = null,
                status = response.status,
                body = responseBody,
            )
        }

        val graphQLResponse = fromJson<GraphQLTweetRoot>(responseBody)
        val tweetResult = graphQLResponse.data?.tweetResult?.result
            ?: throw InternalUtility.handleError(
                exception = null,
                body = "Tweet not found or unavailable",
            )

        val tweet = parseTweetResult(tweetResult)
        return Response(tweet, responseBody)
    }

    override fun getTweetBlocking(
        tweetId: String
    ): Response<Tweet> {
        return toBlocking {
            getTweet(tweetId)
        }
    }

    private fun parseTweetResult(tweetResult: TweetResult): Tweet {
        val legacy = tweetResult.legacy

        // Build User model
        // TweetResultByRestId returns user info in both core and legacy fields.
        // screen_name/name are in UserResult.core, other details in UserResult.legacy.
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
        val mediaEntities = legacy?.extendedEntities?.media
            ?: legacy?.entities?.media
            ?: emptyList()

        val mediaList = mediaEntities.map { entity ->
            Media(
                type = entity.type,
                url = entity.mediaUrlHttps,
                width = entity.originalInfo?.width,
                height = entity.originalInfo?.height,
            )
        }

        // Parse view count
        val viewCount = tweetResult.views?.count?.toLongOrNull()

        return Tweet(
            id = tweetResult.restId,
            text = legacy?.fullText,
            createdAt = legacy?.createdAt,
            user = user,
            replyCount = legacy?.replyCount,
            retweetCount = legacy?.retweetCount,
            favoriteCount = legacy?.favoriteCount,
            media = mediaList,
            viewCount = viewCount,
            inReplyToStatusId = legacy?.inReplyToStatusIdStr,
            conversationId = legacy?.conversationIdStr,
            lang = legacy?.lang,
        )
    }

    private object InternalUtility {
        fun handleError(
            exception: Exception?,
            status: Int? = null,
            body: String? = null,
        ) = work.socialhub.kxweb.internal.share.InternalUtility.handleError(
            exception, status, body,
        )
    }
}
