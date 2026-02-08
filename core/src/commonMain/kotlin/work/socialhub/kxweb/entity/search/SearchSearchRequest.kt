package work.socialhub.kxweb.entity.search

import kotlin.js.JsExport

@JsExport
class SearchSearchRequest {
    var query: String? = null
    var count: Int = 20
    var searchType: SearchType = SearchType.LATEST
    var cursor: String? = null
}
