package work.socialhub.kxweb.entity.bookmark

import kotlin.js.JsExport

@JsExport
class SearchBookmarksRequest {
    var query: String? = null
    var count: Int = 20
    var cursor: String? = null
}
