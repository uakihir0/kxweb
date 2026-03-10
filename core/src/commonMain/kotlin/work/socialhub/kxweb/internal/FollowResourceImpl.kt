package work.socialhub.kxweb.internal

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import work.socialhub.kxweb.XWebConfig
import work.socialhub.kxweb.XWebException
import work.socialhub.kxweb.api.FollowResource
import work.socialhub.kxweb.domain.QueryId
import work.socialhub.kxweb.domain.Service
import work.socialhub.kxweb.entity.follow.FollowRequest
import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.internal.entity.GraphQLMutationRoot
import work.socialhub.kxweb.internal.share.InternalUtility
import work.socialhub.kxweb.internal.share.InternalUtility.fromJson
import work.socialhub.kxweb.internal.share.InternalUtility.graphqlPostBody
import work.socialhub.kxweb.internal.share.InternalUtility.graphqlUrl
import work.socialhub.kxweb.internal.share.InternalUtility.httpRequest
import work.socialhub.kxweb.internal.share.InternalUtility.setTimeouts
import work.socialhub.kxweb.internal.share.InternalUtility.withAuthHeaders
import work.socialhub.kxweb.internal.share.InternalUtility.withCookieHeaders
import work.socialhub.kxweb.model.MutationResult
import work.socialhub.kxweb.util.toBlocking

class FollowResourceImpl(
    private val config: XWebConfig
) : FollowResource {

    override suspend fun follow(request: FollowRequest): Response<MutationResult> {
        return try {
            mutateViaGraphQL(request.userId, QueryId.CREATE_FRIENDSHIP, "CreateFriendship")
        } catch (e: XWebException) {
            mutateViaRest(request.userId, "create")
        }
    }

    override fun followBlocking(request: FollowRequest): Response<MutationResult> =
        toBlocking { follow(request) }

    override suspend fun unfollow(request: FollowRequest): Response<MutationResult> {
        return try {
            mutateViaGraphQL(request.userId, QueryId.DESTROY_FRIENDSHIP, "DestroyFriendship")
        } catch (e: XWebException) {
            mutateViaRest(request.userId, "destroy")
        }
    }

    override fun unfollowBlocking(request: FollowRequest): Response<MutationResult> =
        toBlocking { unfollow(request) }

    private suspend fun mutateViaGraphQL(
        userId: String?,
        queryId: String,
        operationName: String,
    ): Response<MutationResult> {
        val url = graphqlUrl(config, queryId, operationName)

        val variables = buildJsonObject {
            userId?.let { put("user_id", it) }
        }.toString()

        val postBody = graphqlPostBody(variables, queryId)

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

    private suspend fun mutateViaRest(
        userId: String?,
        action: String,
    ): Response<MutationResult> {
        val url = "${Service.X_REST_API.uri}/1.1/friendships/$action.json"

        val httpRequest = httpRequest(config)
            .url(url)
            .setTimeouts(config)
            .header("content-type", "application/x-www-form-urlencoded")
            .withCookieHeaders(config)

        userId?.let {
            httpRequest.param("user_id", it)
            httpRequest.param("skip_status", "true")
        }

        val response = httpRequest.post()
        val body = response.stringBody

        if (response.status !in 200..299) {
            throw InternalUtility.handleError(null, response.status, body)
        }

        return Response(MutationResult(success = true), body)
    }
}
