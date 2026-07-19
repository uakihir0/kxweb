package work.socialhub.kxweb.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

/**
 * A single trending topic on X (Twitter).
 */
@Serializable
@JsExport
data class Trend(
    /** Display name of the trend (e.g. a hashtag or phrase). */
    var name: String? = null,

    /** Search query string used to open the trend. */
    var query: String? = null,

    /** URL to the trend's search results. */
    var url: String? = null,

    /** Approximate number of tweets, or null when unavailable. */
    var tweetVolume: Long? = null,
)
