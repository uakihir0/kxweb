package work.socialhub.kxweb.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
data class Trend(
    var name: String? = null,
    var query: String? = null,
    var url: String? = null,
    var tweetVolume: Long? = null,
)
