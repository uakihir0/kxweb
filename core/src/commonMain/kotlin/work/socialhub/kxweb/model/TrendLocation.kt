package work.socialhub.kxweb.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
data class TrendLocation(
    var name: String? = null,
    var woeid: Long? = null,
    var country: String? = null,
    var countryCode: String? = null,
    var placeType: String? = null,
    var parentId: Long? = null,
    var url: String? = null,
)
