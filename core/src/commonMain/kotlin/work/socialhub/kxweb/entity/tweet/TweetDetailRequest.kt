package work.socialhub.kxweb.entity.tweet

import kotlin.js.JsExport

@JsExport
class TweetDetailRequest {
    var tweetId: String? = null
    var cursor: String? = null
}
