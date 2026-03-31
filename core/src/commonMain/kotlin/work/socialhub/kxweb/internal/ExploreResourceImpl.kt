package work.socialhub.kxweb.internal

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import work.socialhub.kxweb.XWebConfig
import work.socialhub.kxweb.api.ExploreResource
import work.socialhub.kxweb.domain.QueryId
import work.socialhub.kxweb.entity.explore.GetNewsRequest
import work.socialhub.kxweb.entity.explore.GetNewsResponse
import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.internal.entity.GraphQLExploreRoot
import work.socialhub.kxweb.internal.share.InternalUtility
import work.socialhub.kxweb.internal.share.InternalUtility.exploreFeatures
import work.socialhub.kxweb.internal.share.InternalUtility.fromJson
import work.socialhub.kxweb.internal.share.InternalUtility.graphqlUrl
import work.socialhub.kxweb.internal.share.InternalUtility.httpRequest
import work.socialhub.kxweb.internal.share.InternalUtility.setTimeouts
import work.socialhub.kxweb.internal.share.InternalUtility.trackResponse
import work.socialhub.kxweb.internal.share.InternalUtility.withAuthHeaders
import work.socialhub.kxweb.internal.share.TweetParser
import work.socialhub.kxweb.util.toBlocking

class ExploreResourceImpl(
    private val config: XWebConfig
) : ExploreResource {

    override suspend fun getNews(
        request: GetNewsRequest
    ): Response<GetNewsResponse> {
        val url = graphqlUrl(config, QueryId.GENERIC_TIMELINE_BY_ID, "GenericTimelineById")

        val timelineId = request.tab?.timelineId ?: "trends_for_you"
        val variables = buildJsonObject {
            put("timelineId", timelineId)
            put("count", request.count)
            request.cursor?.let { put("cursor", it) }
        }

        val features = buildJsonObject {
            exploreFeatures().forEach { (k, v) -> put(k, v) }
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
        trackResponse(config, "GenericTimelineById", response)
        val body = response.stringBody

        if (response.status !in 200..299) {
            throw InternalUtility.handleError(null, response.status, body)
        }

        val root = fromJson<GraphQLExploreRoot>(body)
        val instructions = root.data?.timeline_by_id?.timeline?.instructions ?: emptyList()
        val result = TweetParser.parseTimelineInstructions(instructions)

        return Response(
            GetNewsResponse(tweets = result.tweets, cursor = result.cursor),
            body,
        )
    }

    override fun getNewsBlocking(
        request: GetNewsRequest
    ): Response<GetNewsResponse> = toBlocking { getNews(request) }
}
