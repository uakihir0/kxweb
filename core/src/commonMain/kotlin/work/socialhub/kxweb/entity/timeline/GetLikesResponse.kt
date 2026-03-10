package work.socialhub.kxweb.entity.timeline

import work.socialhub.kxweb.model.Tweet
import kotlin.js.JsExport

@JsExport
data class GetLikesResponse(
    var tweets: List<Tweet> = emptyList(),
    var cursor: String? = null,
)
