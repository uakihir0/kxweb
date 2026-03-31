package work.socialhub.kxweb.internal

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import work.socialhub.kxweb.XWebConfig
import work.socialhub.kxweb.api.ListResource
import work.socialhub.kxweb.domain.QueryId
import work.socialhub.kxweb.entity.list.GetListsRequest
import work.socialhub.kxweb.entity.list.ListTimelineRequest
import work.socialhub.kxweb.entity.list.ListTimelineResponse
import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.internal.entity.GraphQLListTimelineRoot
import work.socialhub.kxweb.internal.entity.GraphQLListsRoot
import work.socialhub.kxweb.internal.share.InternalUtility
import work.socialhub.kxweb.internal.share.InternalUtility.fromJson
import work.socialhub.kxweb.internal.share.InternalUtility.graphqlUrl
import work.socialhub.kxweb.internal.share.InternalUtility.httpRequest
import work.socialhub.kxweb.internal.share.InternalUtility.listsFeatures
import work.socialhub.kxweb.internal.share.InternalUtility.setTimeouts
import work.socialhub.kxweb.internal.share.InternalUtility.trackResponse
import work.socialhub.kxweb.internal.share.InternalUtility.withAuthHeaders
import work.socialhub.kxweb.internal.share.TweetParser
import work.socialhub.kxweb.util.toBlocking

class ListResourceImpl(
    private val config: XWebConfig
) : ListResource {

    override suspend fun getOwnedLists(
        request: GetListsRequest
    ): Response<ListTimelineResponse> {
        return fetchLists(request, QueryId.LIST_OWNERSHIPS, "ListOwnerships")
    }

    override fun getOwnedListsBlocking(
        request: GetListsRequest
    ): Response<ListTimelineResponse> = toBlocking { getOwnedLists(request) }

    override suspend fun getListMemberships(
        request: GetListsRequest
    ): Response<ListTimelineResponse> {
        return fetchLists(request, QueryId.LIST_MEMBERSHIPS, "ListMemberships")
    }

    override fun getListMembershipsBlocking(
        request: GetListsRequest
    ): Response<ListTimelineResponse> = toBlocking { getListMemberships(request) }

    override suspend fun getListTimeline(
        request: ListTimelineRequest
    ): Response<ListTimelineResponse> {
        val url = graphqlUrl(config, QueryId.LIST_LATEST_TWEETS_TIMELINE, "ListLatestTweetsTimeline")

        val variables = buildJsonObject {
            request.listId?.let { put("listId", it) }
            put("count", request.count)
            request.cursor?.let { put("cursor", it) }
        }

        val features = buildJsonObject {
            listsFeatures().forEach { (k, v) -> put(k, v) }
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
        trackResponse(config, "ListLatestTweetsTimeline", response)
        val body = response.stringBody

        if (response.status !in 200..299) {
            throw InternalUtility.handleError(null, response.status, body)
        }

        val root = fromJson<GraphQLListTimelineRoot>(body)
        val instructions = root.data?.list?.tweetsTimeline?.timeline?.instructions ?: emptyList()
        val result = TweetParser.parseTimelineInstructions(instructions)

        return Response(
            ListTimelineResponse(tweets = result.tweets, cursor = result.cursor),
            body,
        )
    }

    override fun getListTimelineBlocking(
        request: ListTimelineRequest
    ): Response<ListTimelineResponse> = toBlocking { getListTimeline(request) }

    private suspend fun fetchLists(
        request: GetListsRequest,
        queryId: String,
        operationName: String,
    ): Response<ListTimelineResponse> {
        val url = graphqlUrl(config, queryId, operationName)

        val variables = buildJsonObject {
            request.userId?.let { put("userId", it) }
            put("count", request.count)
            request.cursor?.let { put("cursor", it) }
        }

        val features = buildJsonObject {
            listsFeatures().forEach { (k, v) -> put(k, v) }
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
        trackResponse(config, operationName, response)
        val body = response.stringBody

        if (response.status !in 200..299) {
            throw InternalUtility.handleError(null, response.status, body)
        }

        val root = fromJson<GraphQLListsRoot>(body)
        val instructions = root.data?.user?.result?.timeline?.timeline?.instructions ?: emptyList()
        val result = TweetParser.parseTimelineInstructions(instructions)

        return Response(
            ListTimelineResponse(tweets = result.tweets, cursor = result.cursor),
            body,
        )
    }
}
