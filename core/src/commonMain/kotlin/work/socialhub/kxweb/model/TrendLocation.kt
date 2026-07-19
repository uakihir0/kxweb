package work.socialhub.kxweb.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

/**
 * A location for which trends are available, identified by its WOEID
 * (Yahoo! Where On Earth ID).
 */
@Serializable
@JsExport
data class TrendLocation(
    /** Display name of the location (e.g. "Worldwide", "Tokyo"). */
    var name: String? = null,

    /** Yahoo! Where On Earth ID; pass to getTrends to fetch its trends. */
    var woeid: Long? = null,

    /** Country name, or empty for supernational locations. */
    var country: String? = null,

    /** ISO country code, or null when not applicable. */
    var countryCode: String? = null,

    /** Type of place (e.g. "Town", "Country", "Supername"). */
    var placeType: String? = null,

    /** WOEID of the parent location, or null. */
    var parentId: Long? = null,

    /** Yahoo! GeoPlanet URL describing the location. */
    var url: String? = null,
)
