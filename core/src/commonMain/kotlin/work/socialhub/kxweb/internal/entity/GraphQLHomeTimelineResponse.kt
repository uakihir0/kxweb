package work.socialhub.kxweb.internal.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GraphQLHomeTimelineRoot(
    val data: GraphQLHomeTimelineData? = null,
)

@Serializable
data class GraphQLHomeTimelineData(
    val home: HomeTimelineHome? = null,
)

@Serializable
data class HomeTimelineHome(
    @SerialName("home_timeline_urt")
    val homeTimelineUrt: Timeline? = null,
)
