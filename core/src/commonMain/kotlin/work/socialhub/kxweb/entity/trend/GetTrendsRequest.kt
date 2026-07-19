package work.socialhub.kxweb.entity.trend

import kotlin.js.JsExport

/**
 * Request for [work.socialhub.kxweb.api.TrendResource.getTrends].
 * Maps to the v1.1 trends/place.json endpoint.
 */
@JsExport
class GetTrendsRequest {
    /** Yahoo! Where On Earth ID of the location. 1 = Worldwide. */
    var woeid: Long = 1
}
