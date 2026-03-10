package work.socialhub.kxweb.internal

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import work.socialhub.kxweb.XWebConfig
import work.socialhub.kxweb.api.BookmarkResource
import work.socialhub.kxweb.domain.QueryId
import work.socialhub.kxweb.entity.bookmark.BookmarkFolderTimelineRequest
import work.socialhub.kxweb.entity.bookmark.BookmarkRequest
import work.socialhub.kxweb.entity.bookmark.GetBookmarksRequest
import work.socialhub.kxweb.entity.bookmark.GetBookmarksResponse
import work.socialhub.kxweb.entity.bookmark.SearchBookmarksRequest
import work.socialhub.kxweb.entity.search.SearchSearchResponse
import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.internal.entity.GraphQLBookmarksRoot
import work.socialhub.kxweb.internal.entity.GraphQLMutationRoot
import work.socialhub.kxweb.internal.entity.GraphQLSearchRoot
import work.socialhub.kxweb.internal.share.InternalUtility
import work.socialhub.kxweb.internal.share.InternalUtility.bookmarksFeatures
import work.socialhub.kxweb.internal.share.InternalUtility.fromJson
import work.socialhub.kxweb.internal.share.InternalUtility.graphqlPostBody
import work.socialhub.kxweb.internal.share.InternalUtility.graphqlUrl
import work.socialhub.kxweb.internal.share.InternalUtility.httpRequest
import work.socialhub.kxweb.internal.share.InternalUtility.searchFeatures
import work.socialhub.kxweb.internal.share.InternalUtility.setTimeouts
import work.socialhub.kxweb.internal.share.InternalUtility.withAuthHeaders
import work.socialhub.kxweb.internal.share.TweetParser
import work.socialhub.kxweb.model.MutationResult
import work.socialhub.kxweb.util.toBlocking

class BookmarkResourceImpl(
    private val config: XWebConfig
) : BookmarkResource {

    override suspend fun getBookmarks(
        request: GetBookmarksRequest
    ): Response<GetBookmarksResponse> {
        val url = graphqlUrl(config, QueryId.BOOKMARKS, "Bookmarks")

        val variables = buildJsonObject {
            put("count", request.count)
            request.cursor?.let { put("cursor", it) }
        }

        val features = buildJsonObject {
            bookmarksFeatures().forEach { (k, v) -> put(k, v) }
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

        val root = fromJson<GraphQLBookmarksRoot>(body)
        val instructions = root.data?.bookmarkTimeline?.timeline?.instructions ?: emptyList()
        val result = TweetParser.parseTimelineInstructions(instructions)

        return Response(
            GetBookmarksResponse(tweets = result.tweets, cursor = result.cursor),
            body,
        )
    }

    override fun getBookmarksBlocking(
        request: GetBookmarksRequest
    ): Response<GetBookmarksResponse> = toBlocking { getBookmarks(request) }

    override suspend fun searchBookmarks(
        request: SearchBookmarksRequest
    ): Response<SearchSearchResponse> {
        val url = graphqlUrl(config, QueryId.SEARCH_TIMELINE, "SearchTimeline")

        val rawQuery = "${request.query ?: ""} filter:bookmarks"
        val variables = buildJsonObject {
            put("rawQuery", rawQuery.trim())
            put("count", request.count)
            put("querySource", "typed_query")
            put("product", "Latest")
            request.cursor?.let { put("cursor", it) }
        }

        val features = buildJsonObject {
            searchFeatures().forEach { (k, v) -> put(k, v) }
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

        val graphQLResponse = fromJson<GraphQLSearchRoot>(body)
        val instructions = graphQLResponse.data
            ?.searchByRawQuery
            ?.searchTimeline
            ?.timeline
            ?.instructions
            ?: return Response(SearchSearchResponse(), body)

        val result = TweetParser.parseTimelineInstructions(instructions)

        return Response(
            SearchSearchResponse(tweets = result.tweets, cursor = result.cursor),
            body,
        )
    }

    override fun searchBookmarksBlocking(
        request: SearchBookmarksRequest
    ): Response<SearchSearchResponse> = toBlocking { searchBookmarks(request) }

    override suspend fun bookmark(
        request: BookmarkRequest
    ): Response<MutationResult> {
        return mutateBookmark(request, QueryId.CREATE_BOOKMARK, "CreateBookmark")
    }

    override fun bookmarkBlocking(
        request: BookmarkRequest
    ): Response<MutationResult> = toBlocking { bookmark(request) }

    override suspend fun unbookmark(
        request: BookmarkRequest
    ): Response<MutationResult> {
        return mutateBookmark(request, QueryId.DELETE_BOOKMARK, "DeleteBookmark")
    }

    override fun unbookmarkBlocking(
        request: BookmarkRequest
    ): Response<MutationResult> = toBlocking { unbookmark(request) }

    override suspend fun getBookmarkFolderTimeline(
        request: BookmarkFolderTimelineRequest
    ): Response<GetBookmarksResponse> {
        val url = graphqlUrl(config, QueryId.BOOKMARK_FOLDER_TIMELINE, "BookmarkFolderTimeline")

        val variables = buildJsonObject {
            request.folderId?.let { put("bookmark_collection_id", it) }
            put("count", request.count)
            request.cursor?.let { put("cursor", it) }
        }

        val features = buildJsonObject {
            bookmarksFeatures().forEach { (k, v) -> put(k, v) }
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

        val root = fromJson<GraphQLBookmarksRoot>(body)
        val instructions = root.data?.bookmarkTimeline?.timeline?.instructions ?: emptyList()
        val result = TweetParser.parseTimelineInstructions(instructions)

        return Response(
            GetBookmarksResponse(tweets = result.tweets, cursor = result.cursor),
            body,
        )
    }

    override fun getBookmarkFolderTimelineBlocking(
        request: BookmarkFolderTimelineRequest
    ): Response<GetBookmarksResponse> = toBlocking { getBookmarkFolderTimeline(request) }

    private suspend fun mutateBookmark(
        request: BookmarkRequest,
        queryId: String,
        operationName: String,
    ): Response<MutationResult> {
        val url = graphqlUrl(config, queryId, operationName)

        val variables = buildJsonObject {
            request.tweetId?.let { put("tweet_id", it) }
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
}
