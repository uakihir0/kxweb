package work.socialhub.kxweb.internal

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import work.socialhub.kxweb.XWebConfig
import work.socialhub.kxweb.api.HomeResource
import work.socialhub.kxweb.domain.QueryId
import work.socialhub.kxweb.entity.home.HomeTimelineRequest
import work.socialhub.kxweb.entity.home.HomeTimelineResponse
import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.internal.entity.GraphQLHomeTimelineRoot
import work.socialhub.kxweb.internal.share.InternalUtility
import work.socialhub.kxweb.internal.share.InternalUtility.fromJson
import work.socialhub.kxweb.internal.share.InternalUtility.graphqlUrl
import work.socialhub.kxweb.internal.share.InternalUtility.homeTimelineFeatures
import work.socialhub.kxweb.internal.share.InternalUtility.httpRequest
import work.socialhub.kxweb.internal.share.InternalUtility.setTimeouts
import work.socialhub.kxweb.internal.share.InternalUtility.withAuthHeaders
import work.socialhub.kxweb.internal.share.TweetParser
import work.socialhub.kxweb.util.toBlocking

class HomeResourceImpl(
    private val config: XWebConfig
) : HomeResource {

    override suspend fun getHomeTimeline(
        request: HomeTimelineRequest
    ): Response<HomeTimelineResponse> {
        return fetchTimeline(request, QueryId.HOME_TIMELINE, "HomeTimeline")
    }

    override fun getHomeTimelineBlocking(
        request: HomeTimelineRequest
    ): Response<HomeTimelineResponse> = toBlocking { getHomeTimeline(request) }

    override suspend fun getHomeLatestTimeline(
        request: HomeTimelineRequest
    ): Response<HomeTimelineResponse> {
        return fetchTimeline(request, QueryId.HOME_LATEST_TIMELINE, "HomeLatestTimeline")
    }

    override fun getHomeLatestTimelineBlocking(
        request: HomeTimelineRequest
    ): Response<HomeTimelineResponse> = toBlocking { getHomeLatestTimeline(request) }

    private suspend fun fetchTimeline(
        request: HomeTimelineRequest,
        queryId: String,
        operationName: String,
    ): Response<HomeTimelineResponse> {
        val url = graphqlUrl(config, queryId, operationName)

        val variables = buildJsonObject {
            put("count", request.count)
            request.cursor?.let { put("cursor", it) }
            put("includePromotedContent", true)
            put("latestControlAvailable", true)
        }

        val features = buildJsonObject {
            homeTimelineFeatures().forEach { (k, v) -> put(k, v) }
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

        val root = fromJson<GraphQLHomeTimelineRoot>(body)
        val instructions = root.data?.home?.homeTimelineUrt?.instructions ?: emptyList()
        val result = TweetParser.parseTimelineInstructions(instructions)

        return Response(
            HomeTimelineResponse(tweets = result.tweets, cursor = result.cursor),
            body,
        )
    }
}
