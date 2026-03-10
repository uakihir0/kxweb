package work.socialhub.kxweb.internal

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import work.socialhub.kxweb.XWebConfig
import work.socialhub.kxweb.api.PostResource
import work.socialhub.kxweb.domain.QueryId
import work.socialhub.kxweb.entity.post.CreateTweetRequest
import work.socialhub.kxweb.entity.post.DeleteTweetRequest
import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.internal.entity.GraphQLMutationRoot
import work.socialhub.kxweb.internal.share.InternalUtility
import work.socialhub.kxweb.internal.share.InternalUtility.fromJson
import work.socialhub.kxweb.internal.share.InternalUtility.graphqlPostBody
import work.socialhub.kxweb.internal.share.InternalUtility.graphqlPostBodyWithFeatures
import work.socialhub.kxweb.internal.share.InternalUtility.graphqlUrl
import work.socialhub.kxweb.internal.share.InternalUtility.httpRequest
import work.socialhub.kxweb.internal.share.InternalUtility.setTimeouts
import work.socialhub.kxweb.internal.share.InternalUtility.tweetCreateFeatures
import work.socialhub.kxweb.internal.share.InternalUtility.withAuthHeaders
import work.socialhub.kxweb.internal.share.TweetParser
import work.socialhub.kxweb.model.MutationResult
import work.socialhub.kxweb.model.Tweet
import work.socialhub.kxweb.util.toBlocking

class PostResourceImpl(
    private val config: XWebConfig
) : PostResource {

    override suspend fun createTweet(
        request: CreateTweetRequest
    ): Response<Tweet> {
        val url = graphqlUrl(config, QueryId.CREATE_TWEET, "CreateTweet")

        val variables = buildJsonObject {
            put("tweet_text", request.text ?: "")
            put("dark_request", false)
            putJsonObject("media") {
                putJsonArray("media_entities") {
                    for (mediaId in request.mediaIds) {
                        add(buildJsonObject {
                            put("media_id", mediaId)
                            put("tagged_users", JsonArray(emptyList()))
                        })
                    }
                }
                put("possibly_sensitive", false)
            }
            putJsonObject("semantic_annotation_ids") {}
            request.replyToTweetId?.let { replyId ->
                putJsonObject("reply") {
                    put("in_reply_to_tweet_id", replyId)
                    putJsonArray("exclude_reply_user_ids") {}
                }
            }
        }.toString()

        val postBody = graphqlPostBodyWithFeatures(variables, tweetCreateFeatures(), QueryId.CREATE_TWEET)

        val httpRequest = httpRequest(config)
            .url(url)
            .setTimeouts(config)
            .header("content-type", "application/json")
            .json(postBody)
            .withAuthHeaders(config, "POST", url)

        val response = httpRequest.post()
        val body = response.stringBody

        if (response.status !in 200..299) {
            throw InternalUtility.handleError(null, response.status, body)
        }

        val root = fromJson<GraphQLMutationRoot>(body)
        val tweetResult = root.data?.createTweet?.tweetResults?.result

        val tweet = if (tweetResult != null) {
            TweetParser.parseTweetResult(tweetResult)
        } else {
            Tweet()
        }

        return Response(tweet, body)
    }

    override fun createTweetBlocking(
        request: CreateTweetRequest
    ): Response<Tweet> = toBlocking { createTweet(request) }

    override suspend fun deleteTweet(
        request: DeleteTweetRequest
    ): Response<MutationResult> {
        val url = graphqlUrl(config, QueryId.DELETE_TWEET, "DeleteTweet")

        val variables = buildJsonObject {
            request.tweetId?.let { put("tweet_id", it) }
            put("dark_request", false)
        }.toString()

        val postBody = graphqlPostBody(variables, QueryId.DELETE_TWEET)

        val httpRequest = httpRequest(config)
            .url(url)
            .setTimeouts(config)
            .header("content-type", "application/json")
            .json(postBody)
            .withAuthHeaders(config, "POST", url)

        val response = httpRequest.post()
        val body = response.stringBody

        if (response.status !in 200..299) {
            throw InternalUtility.handleError(null, response.status, body)
        }

        val root = fromJson<GraphQLMutationRoot>(body)
        val success = root.errors.isNullOrEmpty()

        return Response(
            MutationResult(success = success, error = root.errors?.firstOrNull()?.message),
            body,
        )
    }

    override fun deleteTweetBlocking(
        request: DeleteTweetRequest
    ): Response<MutationResult> = toBlocking { deleteTweet(request) }
}
