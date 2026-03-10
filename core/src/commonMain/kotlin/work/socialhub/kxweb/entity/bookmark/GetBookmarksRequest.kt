package work.socialhub.kxweb.entity.bookmark

import kotlin.js.JsExport

@JsExport
class GetBookmarksRequest {
    var count: Int = 20
    var cursor: String? = null
}
