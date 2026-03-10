package work.socialhub.kxweb.internal.entity

import kotlinx.serialization.Serializable

@Serializable
data class GraphQLFollowingRoot(
    val data: GraphQLFollowingData? = null,
)

@Serializable
data class GraphQLFollowingData(
    val user: GraphQLFollowingUser? = null,
)

@Serializable
data class GraphQLFollowingUser(
    val result: GraphQLFollowingResult? = null,
)

@Serializable
data class GraphQLFollowingResult(
    val timeline: GraphQLFollowingTimeline? = null,
)

@Serializable
data class GraphQLFollowingTimeline(
    val timeline: Timeline? = null,
)
