package work.socialhub.kxweb.entity.search

import kotlin.js.JsExport

@JsExport
class SearchUsersRequest {
    var query: String? = null
    var count: Int = 20
    var cursor: String? = null
}
