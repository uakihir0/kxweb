package work.socialhub.kxweb.internal

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import work.socialhub.kxweb.XWebConfig
import work.socialhub.kxweb.api.TimelineResource
import work.socialhub.kxweb.domain.QueryId
import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.entity.timeline.GetLikesRequest
import work.socialhub.kxweb.entity.timeline.GetLikesResponse
import work.socialhub.kxweb.internal.entity.GraphQLLikesRoot
import work.socialhub.kxweb.internal.share.InternalUtility
import work.socialhub.kxweb.internal.share.InternalUtility.fromJson
import work.socialhub.kxweb.internal.share.InternalUtility.graphqlUrl
import work.socialhub.kxweb.internal.share.InternalUtility.httpRequest
import work.socialhub.kxweb.internal.share.InternalUtility.likesFeatures
import work.socialhub.kxweb.internal.share.InternalUtility.setTimeouts
import work.socialhub.kxweb.internal.share.InternalUtility.trackResponse
import work.socialhub.kxweb.internal.share.InternalUtility.withAuthHeaders
import work.socialhub.kxweb.internal.share.TweetParser
import work.socialhub.kxweb.util.toBlocking

class TimelineResourceImpl(
    private val config: XWebConfig
) : TimelineResource {

    override suspend fun getLikes(
        request: GetLikesRequest
    ): Response<GetLikesResponse> {
        val url = graphqlUrl(config, QueryId.LIKES, "Likes")

        val variables = buildJsonObject {
            request.userId?.let { put("userId", it) }
            put("count", request.count)
            request.cursor?.let { put("cursor", it) }
            put("includePromotedContent", false)
        }

        val features = buildJsonObject {
            likesFeatures().forEach { (k, v) -> put(k, v) }
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
        trackResponse(config, "Likes", response)
        val body = response.stringBody

        if (response.status !in 200..299) {
            throw InternalUtility.handleError(null, response.status, body)
        }

        val root = fromJson<GraphQLLikesRoot>(body)
        val instructions = root.data?.user?.result?.timeline_v2?.timeline?.instructions ?: emptyList()
        val result = TweetParser.parseTimelineInstructions(instructions)

        return Response(
            GetLikesResponse(tweets = result.tweets, cursor = result.cursor),
            body,
        )
    }

    override fun getLikesBlocking(
        request: GetLikesRequest
    ): Response<GetLikesResponse> = toBlocking { getLikes(request) }
}
