package work.socialhub.kxweb.entity.post

import kotlin.js.JsExport

@JsExport
class CreateTweetRequest {
    var text: String? = null
    var mediaIds: List<String> = emptyList()
    var replyToTweetId: String? = null
}
