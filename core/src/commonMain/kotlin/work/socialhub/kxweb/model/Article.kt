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
    /** Article identifier. */
    var id: String? = null,

    /** Article title. */
    var title: String? = null,

    /** Short preview/summary text. */
    var previewText: String? = null,

    /** Full article body as plain text (present only when requested). */
    var plainText: String? = null,

    /** URL of the article's cover image. */
    var coverImageUrl: String? = null,
)
