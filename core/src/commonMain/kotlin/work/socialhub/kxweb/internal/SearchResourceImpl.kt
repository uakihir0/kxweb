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
import work.socialhub.kxweb.internal.share.InternalUtility
import work.socialhub.kxweb.internal.share.InternalUtility.fromJson
import work.socialhub.kxweb.internal.share.InternalUtility.graphqlUrl
import work.socialhub.kxweb.internal.share.InternalUtility.httpRequest
import work.socialhub.kxweb.internal.share.InternalUtility.searchFeatures
import work.socialhub.kxweb.internal.share.InternalUtility.setTimeouts
import work.socialhub.kxweb.internal.share.InternalUtility.trackResponse
import work.socialhub.kxweb.internal.share.InternalUtility.withAuthHeaders
import work.socialhub.kxweb.internal.share.TweetParser
import work.socialhub.kxweb.util.toBlocking

class SearchResourceImpl(
    private val config: XWebConfig
) : SearchResource {

    override suspend fun searchTweets(
        request: SearchSearchRequest
    ): Response<SearchSearchResponse> {
        val url = graphqlUrl(config, QueryId.SEARCH_TIMELINE, "SearchTimeline")

        val variables = buildJsonObject {
            request.query?.let { put("rawQuery", it) }
            put("count", request.count)
            put("querySource", "typed_query")
            put("product", request.searchType.product)
            request.cursor?.let { put("cursor", it) }
        }

        val features = buildJsonObject {
            searchFeatures().forEach { (key, value) -> put(key, value) }
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
        trackResponse(config, "SearchTimeline", response)
        val responseBody = response.stringBody

        if (response.status !in 200..299) {
            throw InternalUtility.handleError(null, response.status, responseBody)
        }

        val graphQLResponse = fromJson<GraphQLSearchRoot>(responseBody)
        val instructions = graphQLResponse.data
            ?.searchByRawQuery
            ?.searchTimeline
            ?.timeline
            ?.instructions
            ?: return Response(SearchSearchResponse(), responseBody)

        val result = TweetParser.parseTimelineInstructions(instructions)

        return Response(
            SearchSearchResponse(tweets = result.tweets, cursor = result.cursor),
            responseBody,
        )
    }

    override fun searchTweetsBlocking(
        request: SearchSearchRequest
    ): Response<SearchSearchResponse> = toBlocking { searchTweets(request) }
}
