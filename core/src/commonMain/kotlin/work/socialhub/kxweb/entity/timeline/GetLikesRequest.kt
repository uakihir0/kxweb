package work.socialhub.kxweb.entity.timeline

import kotlin.js.JsExport

@JsExport
class GetLikesRequest {
    var userId: String? = null
    var count: Int = 20
    var cursor: String? = null
}
