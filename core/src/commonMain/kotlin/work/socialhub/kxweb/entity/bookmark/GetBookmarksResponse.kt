package work.socialhub.kxweb.entity.bookmark

import work.socialhub.kxweb.model.Tweet
import kotlin.js.JsExport

@JsExport
data class GetBookmarksResponse(
    var tweets: List<Tweet> = emptyList(),
    var cursor: String? = null,
)
