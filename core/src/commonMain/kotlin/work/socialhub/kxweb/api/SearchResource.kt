package work.socialhub.kxweb.api

import work.socialhub.kxweb.entity.search.SearchSearchRequest
import work.socialhub.kxweb.entity.search.SearchSearchResponse
import work.socialhub.kxweb.entity.search.SearchUsersRequest
import work.socialhub.kxweb.entity.search.SearchUsersResponse
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

    /**
     * Search for users matching the given query.
     * Uses X (Twitter) GraphQL SearchTimeline endpoint with the "People" product.
     *
     * Note: requires a logged-in session (cookie or OAuth); guest access is
     * not permitted for search.
     */
    suspend fun searchUsers(
        request: SearchUsersRequest
    ): Response<SearchUsersResponse>

    @JsExport.Ignore
    fun searchUsersBlocking(
        request: SearchUsersRequest
    ): Response<SearchUsersResponse>
}
