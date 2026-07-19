package work.socialhub.kxweb.internal.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Internal models for the X (Twitter) v1.1 trends REST endpoints.
 *
 * - /1.1/trends/available.json → List<RestTrendLocation>
 * - /1.1/trends/place.json?id={woeid} → List<RestTrendPlace> (single element)
 */

@Serializable
data class RestTrendLocation(
    val name: String? = null,
    val woeid: Long? = null,
    val country: String? = null,
    val countryCode: String? = null,
    val parentid: Long? = null,
    val url: String? = null,
    val placeType: RestPlaceType? = null,
)

@Serializable
data class RestPlaceType(
    val name: String? = null,
    val code: Int? = null,
)

@Serializable
data class RestTrendPlace(
    val trends: List<RestTrend> = emptyList(),
    @SerialName("as_of")
    val asOf: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    val locations: List<RestTrendLocationRef> = emptyList(),
)

@Serializable
data class RestTrend(
    val name: String? = null,
    val url: String? = null,
    val query: String? = null,
    @SerialName("tweet_volume")
    val tweetVolume: Long? = null,
    // Sometimes an object rather than null; kept generic to avoid failures.
    @SerialName("promoted_content")
    val promotedContent: JsonElement? = null,
)

@Serializable
data class RestTrendLocationRef(
    val name: String? = null,
    val woeid: Long? = null,
)
