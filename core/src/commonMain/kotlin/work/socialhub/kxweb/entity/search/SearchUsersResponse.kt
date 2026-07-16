package work.socialhub.kxweb.entity.search

import work.socialhub.kxweb.model.User
import kotlin.js.JsExport

@JsExport
data class SearchUsersResponse(
    var users: List<User> = emptyList(),
    var cursor: String? = null,
)
