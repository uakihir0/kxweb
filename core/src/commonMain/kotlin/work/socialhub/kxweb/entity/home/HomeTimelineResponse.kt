package work.socialhub.kxweb.entity.home

import work.socialhub.kxweb.model.Tweet
import kotlin.js.JsExport

@JsExport
data class HomeTimelineResponse(
    var tweets: List<Tweet> = emptyList(),
    var cursor: String? = null,
)
