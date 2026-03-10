package work.socialhub.kxweb.entity.account

import kotlin.js.JsExport

@JsExport
data class GetCurrentUserResponse(
    var userId: String? = null,
    var screenName: String? = null,
    var name: String? = null,
)
