package work.socialhub.kxweb.entity.user

import work.socialhub.kxweb.model.Tweet
import kotlin.js.JsExport

@JsExport
data class UserTweetsResponse(
    var tweets: List<Tweet> = emptyList(),
    var cursor: String? = null,
)
