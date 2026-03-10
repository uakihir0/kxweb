package work.socialhub.kxweb.entity.tweet

import work.socialhub.kxweb.model.Tweet
import kotlin.js.JsExport

@JsExport
data class TweetDetailResponse(
    var tweets: List<Tweet> = emptyList(),
    var cursor: String? = null,
)
