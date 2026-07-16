package work.socialhub.kxweb.entity.trend

import work.socialhub.kxweb.model.TrendLocation
import kotlin.js.JsExport

@JsExport
data class GetTrendLocationsResponse(
    var locations: List<TrendLocation> = emptyList(),
)
