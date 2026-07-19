package work.socialhub.kxweb.entity.trend

import work.socialhub.kxweb.model.Trend
import kotlin.js.JsExport

/**
 * Response for [work.socialhub.kxweb.api.TrendResource.getTrends].
 */
@JsExport
data class GetTrendsResponse(
    /** Trending topics for the requested location. */
    var trends: List<Trend> = emptyList(),

    /** Timestamp when the trends were queried (ISO-8601), as reported by X. */
    var asOf: String? = null,

    /** Timestamp when the trends were generated (ISO-8601), as reported by X. */
    var createdAt: String? = null,

    /** Human-readable name of the location the trends apply to. */
    var locationName: String? = null,
)
