package work.socialhub.kxweb.entity.user

import work.socialhub.kxweb.model.User
import kotlin.js.JsExport

@JsExport
data class FollowingResponse(
    var users: List<User> = emptyList(),
    var cursor: String? = null,
)
