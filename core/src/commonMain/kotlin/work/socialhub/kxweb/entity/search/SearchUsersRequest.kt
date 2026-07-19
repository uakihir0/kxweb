package work.socialhub.kxweb.entity.search

import kotlin.js.JsExport

/**
 * Request for [work.socialhub.kxweb.api.SearchResource.searchUsers].
 * Maps to the SearchTimeline GraphQL operation with the "People" product.
 */
@JsExport
class SearchUsersRequest {
    /** The search query string. */
    var query: String? = null

    /** Maximum number of users to return per page. Defaults to 20. */
    var count: Int = 20

    /** Pagination cursor from a previous response; null for the first page. */
    var cursor: String? = null
}
