package work.socialhub.kxweb.entity.explore

import work.socialhub.kxweb.model.Tweet
import kotlin.js.JsExport

@JsExport
data class GetNewsResponse(
    var tweets: List<Tweet> = emptyList(),
    var cursor: String? = null,
)
