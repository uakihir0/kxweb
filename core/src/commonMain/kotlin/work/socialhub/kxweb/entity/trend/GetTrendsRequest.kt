package work.socialhub.kxweb.entity.trend

import kotlin.js.JsExport

@JsExport
class GetTrendsRequest {
    /** Yahoo! Where On Earth ID of the location. 1 = Worldwide. */
    var woeid: Long = 1
}
