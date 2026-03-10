package work.socialhub.kxweb.internal.entity

import kotlinx.serialization.Serializable

@Serializable
data class GraphQLUserTweetsRoot(
    val data: GraphQLUserTweetsData? = null,
)

@Serializable
data class GraphQLUserTweetsData(
    val user: GraphQLUserTweetsUser? = null,
)

@Serializable
data class GraphQLUserTweetsUser(
    val result: GraphQLUserTweetsResult? = null,
)

@Serializable
data class GraphQLUserTweetsResult(
    val timeline_v2: GraphQLUserTimeline? = null,
)

@Serializable
data class GraphQLUserTimeline(
    val timeline: Timeline? = null,
)
