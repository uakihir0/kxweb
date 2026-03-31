package work.socialhub.kxweb.internal

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import work.socialhub.kxweb.XWebConfig
import work.socialhub.kxweb.api.EngagementResource
import work.socialhub.kxweb.domain.QueryId
import work.socialhub.kxweb.entity.engagement.LikeRequest
import work.socialhub.kxweb.entity.engagement.RetweetRequest
import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.internal.entity.GraphQLMutationRoot
import work.socialhub.kxweb.internal.share.InternalUtility
import work.socialhub.kxweb.internal.share.InternalUtility.fromJson
import work.socialhub.kxweb.internal.share.InternalUtility.graphqlPostBody
import work.socialhub.kxweb.internal.share.InternalUtility.graphqlUrl
import work.socialhub.kxweb.internal.share.InternalUtility.httpRequest
import work.socialhub.kxweb.internal.share.InternalUtility.setTimeouts
import work.socialhub.kxweb.internal.share.InternalUtility.trackResponse
import work.socialhub.kxweb.internal.share.InternalUtility.withAuthHeaders
import work.socialhub.kxweb.model.MutationResult
import work.socialhub.kxweb.util.toBlocking

class EngagementResourceImpl(
    private val config: XWebConfig
) : EngagementResource {

    override suspend fun like(request: LikeRequest): Response<MutationResult> {
        return mutate(request.tweetId, QueryId.FAVORITE_TWEET, "FavoriteTweet")
    }

    override fun likeBlocking(request: LikeRequest): Response<MutationResult> =
        toBlocking { like(request) }

    override suspend fun unlike(request: LikeRequest): Response<MutationResult> {
        return mutate(request.tweetId, QueryId.UNFAVORITE_TWEET, "UnfavoriteTweet")
    }

    override fun unlikeBlocking(request: LikeRequest): Response<MutationResult> =
        toBlocking { unlike(request) }

    override suspend fun retweet(request: RetweetRequest): Response<MutationResult> {
        return mutate(request.tweetId, QueryId.CREATE_RETWEET, "CreateRetweet")
    }

    override fun retweetBlocking(request: RetweetRequest): Response<MutationResult> =
        toBlocking { retweet(request) }

    override suspend fun unretweet(request: RetweetRequest): Response<MutationResult> {
        return mutate(request.tweetId, QueryId.DELETE_RETWEET, "DeleteRetweet")
    }

    override fun unretweetBlocking(request: RetweetRequest): Response<MutationResult> =
        toBlocking { unretweet(request) }

    private suspend fun mutate(
        tweetId: String?,
        queryId: String,
        operationName: String,
    ): Response<MutationResult> {
        val url = graphqlUrl(config, queryId, operationName)

        val variables = buildJsonObject {
            tweetId?.let { put("tweet_id", it) }
        }.toString()

        val postBody = graphqlPostBody(variables, queryId)

        val httpRequest = httpRequest(config)
            .url(url)
            .setTimeouts(config)
            .header("content-type", "application/json")
            .json(postBody)
            .withAuthHeaders(config, "POST", url)

        val response = httpRequest.post()
        trackResponse(config, operationName, response)
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
}
