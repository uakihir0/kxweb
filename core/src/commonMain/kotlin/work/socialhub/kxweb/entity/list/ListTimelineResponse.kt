package work.socialhub.kxweb.entity.list

import work.socialhub.kxweb.model.Tweet
import kotlin.js.JsExport

@JsExport
data class ListTimelineResponse(
    var tweets: List<Tweet> = emptyList(),
    var cursor: String? = null,
)
