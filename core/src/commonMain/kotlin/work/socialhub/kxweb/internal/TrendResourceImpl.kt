package work.socialhub.kxweb.internal

import work.socialhub.kxweb.XWebConfig
import work.socialhub.kxweb.api.TrendResource
import work.socialhub.kxweb.domain.Service
import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.entity.trend.GetTrendLocationsResponse
import work.socialhub.kxweb.entity.trend.GetTrendsRequest
import work.socialhub.kxweb.entity.trend.GetTrendsResponse
import work.socialhub.kxweb.internal.entity.RestTrendLocation
import work.socialhub.kxweb.internal.entity.RestTrendPlace
import work.socialhub.kxweb.internal.share.InternalUtility
import work.socialhub.kxweb.internal.share.InternalUtility.fromJson
import work.socialhub.kxweb.internal.share.InternalUtility.httpRequest
import work.socialhub.kxweb.internal.share.InternalUtility.isGuest
import work.socialhub.kxweb.internal.share.InternalUtility.setTimeouts
import work.socialhub.kxweb.internal.share.InternalUtility.trackResponse
import work.socialhub.kxweb.internal.share.InternalUtility.withAuthHeaders
import work.socialhub.kxweb.internal.share.InternalUtility.withGuestRetry
import work.socialhub.kxweb.model.Trend
import work.socialhub.kxweb.model.TrendLocation
import work.socialhub.kxweb.util.toBlocking

class TrendResourceImpl(
    private val config: XWebConfig
) : TrendResource {

    /**
     * Guest requests use the public api.x.com host; cookie/OAuth sessions use
     * the web host that serves the same REST endpoints.
     */
    private fun restBaseUri(): String =
        if (isGuest(config)) Service.X_REST_API_PUBLIC.uri else Service.X_REST_API.uri

    override suspend fun getTrendLocations(): Response<GetTrendLocationsResponse> =
        withGuestRetry(config) {
            val url = "${restBaseUri()}/1.1/trends/available.json"

            val response = httpRequest(config)
                .url(url)
                .setTimeouts(config)
                .withAuthHeaders(config, "GET", url, endpoint = "TrendsAvailable")
                .get()

            trackResponse(config, "TrendsAvailable", response)
            val body = response.stringBody

            if (response.status !in 200..299) {
                throw InternalUtility.handleError(null, response.status, body)
            }

            val locations = fromJson<List<RestTrendLocation>>(body).map {
                TrendLocation(
                    name = it.name,
                    woeid = it.woeid,
                    country = it.country,
                    countryCode = it.countryCode,
                    placeType = it.placeType?.name,
                    parentId = it.parentid,
                    url = it.url,
                )
            }

            Response(GetTrendLocationsResponse(locations = locations), body)
        }

    override fun getTrendLocationsBlocking(): Response<GetTrendLocationsResponse> =
        toBlocking { getTrendLocations() }

    override suspend fun getTrends(
        request: GetTrendsRequest
    ): Response<GetTrendsResponse> = withGuestRetry(config) {
        val url = "${restBaseUri()}/1.1/trends/place.json"
        val idStr = request.woeid.toString()

        val response = httpRequest(config)
            .url(url)
            .setTimeouts(config)
            .query("id", idStr)
            .withAuthHeaders(config, "GET", url, mapOf("id" to idStr), "TrendsPlace")
            .get()

        trackResponse(config, "TrendsPlace", response)
        val body = response.stringBody

        if (response.status !in 200..299) {
            throw InternalUtility.handleError(null, response.status, body)
        }

        val place: RestTrendPlace? = fromJson<List<RestTrendPlace>>(body).firstOrNull()
        val trends = place?.trends?.map {
            Trend(
                name = it.name,
                query = it.query,
                url = it.url,
                tweetVolume = it.tweetVolume,
            )
        } ?: emptyList()

        Response(
            GetTrendsResponse(
                trends = trends,
                asOf = place?.asOf,
                createdAt = place?.createdAt,
                locationName = place?.locations?.firstOrNull()?.name,
            ),
            body,
        )
    }

    override fun getTrendsBlocking(
        request: GetTrendsRequest
    ): Response<GetTrendsResponse> = toBlocking { getTrends(request) }
}
