package work.socialhub.kxweb.api

import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.entity.trend.GetTrendLocationsResponse
import work.socialhub.kxweb.entity.trend.GetTrendsRequest
import work.socialhub.kxweb.entity.trend.GetTrendsResponse
import kotlin.js.JsExport

@JsExport
interface TrendResource {

    /**
     * Get the list of locations for which trends are available.
     * Uses X (Twitter) REST v1.1 trends/available.json endpoint.
     * Works with guest authentication.
     */
    suspend fun getTrendLocations(): Response<GetTrendLocationsResponse>

    @JsExport.Ignore
    fun getTrendLocationsBlocking(): Response<GetTrendLocationsResponse>

    /**
     * Get the trends for a specific location (by WOEID).
     * Uses X (Twitter) REST v1.1 trends/place.json endpoint.
     * Works with guest authentication.
     */
    suspend fun getTrends(
        request: GetTrendsRequest
    ): Response<GetTrendsResponse>

    @JsExport.Ignore
    fun getTrendsBlocking(
        request: GetTrendsRequest
    ): Response<GetTrendsResponse>
}
