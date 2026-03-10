package work.socialhub.kxweb.internal.entity

import kotlinx.serialization.Serializable

@Serializable
data class GraphQLExploreRoot(
    val data: GraphQLExploreData? = null,
)

@Serializable
data class GraphQLExploreData(
    val timeline_by_id: GraphQLExploreTimeline? = null,
)

@Serializable
data class GraphQLExploreTimeline(
    val timeline: Timeline? = null,
)
