package work.socialhub.kxweb.entity.user

import kotlin.js.JsExport

@JsExport
class FollowingRequest {
    var userId: String? = null
    var count: Int = 20
    var cursor: String? = null
}
