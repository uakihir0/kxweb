package work.socialhub.kxweb.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
data class Tweet(
    var id: String? = null,
    var text: String? = null,
    var createdAt: String? = null,
    var user: User? = null,
    var replyCount: Int? = null,
    var retweetCount: Int? = null,
    var favoriteCount: Int? = null,
    var bookmarkCount: Int? = null,
    var quoteCount: Int? = null,
    var media: List<Media> = emptyList(),
    var viewCount: Long? = null,
    var inReplyToStatusId: String? = null,
    var conversationId: String? = null,
    var lang: String? = null,
)
