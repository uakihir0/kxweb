package work.socialhub.kxweb.entity.list

import kotlin.js.JsExport

@JsExport
class ListTimelineRequest {
    var listId: String? = null
    var count: Int = 20
    var cursor: String? = null
}
