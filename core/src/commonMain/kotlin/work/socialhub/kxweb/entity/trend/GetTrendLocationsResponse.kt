package work.socialhub.kxweb.entity.trend

import work.socialhub.kxweb.model.TrendLocation
import kotlin.js.JsExport

/**
 * Response for [work.socialhub.kxweb.api.TrendResource.getTrendLocations].
 */
@JsExport
data class GetTrendLocationsResponse(
    /** Locations for which trends are available, each with its WOEID. */
    var locations: List<TrendLocation> = emptyList(),
)
