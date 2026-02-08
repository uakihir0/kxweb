package work.socialhub.kxweb.entity.search

import work.socialhub.kxweb.model.Tweet
import kotlin.js.JsExport

@JsExport
data class SearchSearchResponse(
    var tweets: List<Tweet> = emptyList(),
    var cursor: String? = null,
)
