package work.socialhub.kxweb.entity.user

import kotlin.js.JsExport

@JsExport
class UserTweetsRequest {
    var userId: String? = null
    var count: Int = 20
    var cursor: String? = null
}
