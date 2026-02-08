package work.socialhub.kxweb.api

import work.socialhub.kxweb.entity.search.SearchSearchRequest
import work.socialhub.kxweb.entity.search.SearchSearchResponse
import work.socialhub.kxweb.entity.share.Response
import kotlin.js.JsExport

@JsExport
interface SearchResource {

    /**
     * Search for tweets matching the given query.
     * Uses X (Twitter) GraphQL SearchTimeline endpoint.
     */
    suspend fun searchTweets(
        request: SearchSearchRequest
    ): Response<SearchSearchResponse>

    @JsExport.Ignore
    fun searchTweetsBlocking(
        request: SearchSearchRequest
    ): Response<SearchSearchResponse>
}
