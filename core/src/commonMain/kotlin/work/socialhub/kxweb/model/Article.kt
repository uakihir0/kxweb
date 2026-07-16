package work.socialhub.kxweb.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

/**
 * An X (Twitter) long-form Article attached to a tweet.
 *
 * Article content is delivered embedded in tweet results when the article
 * field toggles are enabled (see TweetResource.getTweet withArticle=true).
 */
@Serializable
@JsExport
data class Article(
    var id: String? = null,
    var title: String? = null,
    var previewText: String? = null,
    var plainText: String? = null,
    var coverImageUrl: String? = null,
)
