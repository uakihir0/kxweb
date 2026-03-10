package work.socialhub.kxweb.internal.entity

import kotlinx.serialization.Serializable

@Serializable
data class GraphQLLikesRoot(
    val data: GraphQLLikesData? = null,
)

@Serializable
data class GraphQLLikesData(
    val user: GraphQLLikesUser? = null,
)

@Serializable
data class GraphQLLikesUser(
    val result: GraphQLLikesResult? = null,
)

@Serializable
data class GraphQLLikesResult(
    val timeline_v2: GraphQLLikesTimeline? = null,
)

@Serializable
data class GraphQLLikesTimeline(
    val timeline: Timeline? = null,
)
