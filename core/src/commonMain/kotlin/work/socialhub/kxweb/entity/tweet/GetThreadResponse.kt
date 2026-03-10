package work.socialhub.kxweb.entity.tweet

import work.socialhub.kxweb.model.Tweet
import kotlin.js.JsExport

@JsExport
data class GetThreadResponse(
    var tweets: List<Tweet> = emptyList(),
    var cursor: String? = null,
)
