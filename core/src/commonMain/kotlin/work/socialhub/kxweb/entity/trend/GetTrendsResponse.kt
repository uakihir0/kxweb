package work.socialhub.kxweb.entity.trend

import work.socialhub.kxweb.model.Trend
import kotlin.js.JsExport

@JsExport
data class GetTrendsResponse(
    var trends: List<Trend> = emptyList(),
    var asOf: String? = null,
    var createdAt: String? = null,
    var locationName: String? = null,
)
