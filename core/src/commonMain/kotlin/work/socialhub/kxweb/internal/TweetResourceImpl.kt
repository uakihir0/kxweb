package work.socialhub.kxweb.internal

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import work.socialhub.kxweb.XWebConfig
import work.socialhub.kxweb.api.TweetResource
import work.socialhub.kxweb.domain.QueryId
import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.entity.tweet.GetRepliesRequest
import work.socialhub.kxweb.entity.tweet.GetRepliesResponse
import work.socialhub.kxweb.entity.tweet.GetThreadRequest
import work.socialhub.kxweb.entity.tweet.GetThreadResponse
import work.socialhub.kxweb.entity.tweet.TweetDetailRequest
import work.socialhub.kxweb.entity.tweet.TweetDetailResponse
import work.socialhub.kxweb.internal.entity.GraphQLTweetDetailRoot
import work.socialhub.kxweb.internal.entity.GraphQLTweetRoot
import work.socialhub.kxweb.internal.share.InternalUtility
import work.socialhub.kxweb.internal.share.InternalUtility.fromJson
import work.socialhub.kxweb.internal.share.InternalUtility.graphqlUrl
import work.socialhub.kxweb.internal.share.InternalUtility.graphqlUrlPublic
import work.socialhub.kxweb.internal.share.InternalUtility.httpRequest
import work.socialhub.kxweb.internal.share.InternalUtility.setTimeouts
import work.socialhub.kxweb.internal.share.InternalUtility.tweetDetailFeatures
import work.socialhub.kxweb.internal.share.InternalUtility.tweetFeatures
import work.socialhub.kxweb.internal.share.InternalUtility.tweetFieldToggles
import work.socialhub.kxweb.internal.share.InternalUtility.withAuthHeaders
import work.socialhub.kxweb.internal.share.InternalUtility.withBearerHeaders
import work.socialhub.kxweb.internal.share.TweetParser
import work.socialhub.kxweb.model.Tweet
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
            tweetFeatures().forEach { (key, value) -> put(key, value) }
        }

        val fieldToggles = buildJsonObject {
            tweetFieldToggles().forEach { (key, value) -> put(key, value) }
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
            throw InternalUtility.handleError(null, response.status, responseBody)
        }

        val graphQLResponse = fromJson<GraphQLTweetRoot>(responseBody)
        val tweetResult = graphQLResponse.data?.tweetResult?.result
            ?: throw InternalUtility.handleError(null, body = "Tweet not found or unavailable")

        val tweet = TweetParser.parseTweetResult(tweetResult)
        return Response(tweet, responseBody)
    }

    override fun getTweetBlocking(
        tweetId: String
    ): Response<Tweet> = toBlocking { getTweet(tweetId) }

    override suspend fun getTweetDetail(
        request: TweetDetailRequest
    ): Response<TweetDetailResponse> {
        val url = graphqlUrl(config, QueryId.TWEET_DETAIL, "TweetDetail")

        val variables = buildJsonObject {
            request.tweetId?.let { put("focalTweetId", it) }
            put("with_rux_injections", false)
            put("includePromotedContent", true)
            put("withCommunity", true)
            put("withQuickPromoteEligibilityTweetFields", true)
            put("withBirdwatchNotes", true)
            put("withVoice", true)
            put("withV2Timeline", true)
            request.cursor?.let { put("cursor", it) }
        }

        val features = buildJsonObject {
            tweetDetailFeatures().forEach { (k, v) -> put(k, v) }
        }

        val variablesStr = variables.toString()
        val featuresStr = features.toString()
        val queryParams = mapOf("variables" to variablesStr, "features" to featuresStr)

        val httpRequest = httpRequest(config)
            .url(url)
            .setTimeouts(config)
            .query("variables", variablesStr)
            .query("features", featuresStr)
            .withAuthHeaders(config, "GET", url, queryParams)

        val response = httpRequest.get()
        val body = response.stringBody

        if (response.status !in 200..299) {
            throw InternalUtility.handleError(null, response.status, body)
        }

        val root = fromJson<GraphQLTweetDetailRoot>(body)
        val instructions = root.data?.threadedConversation?.instructions ?: emptyList()
        val result = TweetParser.parseTimelineInstructions(instructions)

        return Response(
            TweetDetailResponse(tweets = result.tweets, cursor = result.cursor),
            body,
        )
    }

    override fun getTweetDetailBlocking(
        request: TweetDetailRequest
    ): Response<TweetDetailResponse> = toBlocking { getTweetDetail(request) }

    override suspend fun getReplies(
        request: GetRepliesRequest
    ): Response<GetRepliesResponse> {
        val detailRequest = TweetDetailRequest().also {
            it.tweetId = request.tweetId
            it.cursor = request.cursor
        }
        val detailResponse = getTweetDetail(detailRequest)

        val replies = detailResponse.data.tweets.filter { tweet ->
            tweet.inReplyToStatusId == request.tweetId
        }

        return Response(
            GetRepliesResponse(replies = replies, cursor = detailResponse.data.cursor),
            detailResponse.json,
        )
    }

    override fun getRepliesBlocking(
        request: GetRepliesRequest
    ): Response<GetRepliesResponse> = toBlocking { getReplies(request) }

    override suspend fun getThread(
        request: GetThreadRequest
    ): Response<GetThreadResponse> {
        val detailRequest = TweetDetailRequest().also {
            it.tweetId = request.tweetId
            it.cursor = request.cursor
        }
        val detailResponse = getTweetDetail(detailRequest)

        val allTweets = detailResponse.data.tweets
        val focalTweet = allTweets.find { it.id == request.tweetId }
        val conversationId = focalTweet?.conversationId ?: request.tweetId

        val threadTweets = allTweets
            .filter { it.conversationId == conversationId }
            .sortedBy { it.createdAt }

        return Response(
            GetThreadResponse(tweets = threadTweets, cursor = detailResponse.data.cursor),
            detailResponse.json,
        )
    }

    override fun getThreadBlocking(
        request: GetThreadRequest
    ): Response<GetThreadResponse> = toBlocking { getThread(request) }
}
