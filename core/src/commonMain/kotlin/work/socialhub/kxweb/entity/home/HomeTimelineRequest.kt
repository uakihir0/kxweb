package work.socialhub.kxweb.entity.home

import kotlin.js.JsExport

@JsExport
class HomeTimelineRequest {
    var count: Int = 20
    var cursor: String? = null
}
