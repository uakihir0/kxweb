package work.socialhub.kxweb.entity.search

import work.socialhub.kxweb.model.User
import kotlin.js.JsExport

/**
 * Response for [work.socialhub.kxweb.api.SearchResource.searchUsers].
 */
@JsExport
data class SearchUsersResponse(
    /** Users matching the query for this page. */
    var users: List<User> = emptyList(),

    /** Cursor for the next page, or null if there are no more results. */
    var cursor: String? = null,
)
