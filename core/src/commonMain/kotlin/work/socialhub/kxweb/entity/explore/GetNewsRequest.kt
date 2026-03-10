package work.socialhub.kxweb.entity.explore

import kotlin.js.JsExport

@JsExport
class GetNewsRequest {
    var count: Int = 20
    var cursor: String? = null
    var tab: ExploreTab? = null
}
