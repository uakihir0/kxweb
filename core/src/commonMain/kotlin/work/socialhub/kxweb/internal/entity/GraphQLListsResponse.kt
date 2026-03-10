package work.socialhub.kxweb.internal.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GraphQLListsRoot(
    val data: GraphQLListsData? = null,
)

@Serializable
data class GraphQLListsData(
    val user: GraphQLListsUser? = null,
)

@Serializable
data class GraphQLListsUser(
    val result: GraphQLListsResult? = null,
)

@Serializable
data class GraphQLListsResult(
    val timeline: GraphQLListsTimeline? = null,
)

@Serializable
data class GraphQLListsTimeline(
    val timeline: Timeline? = null,
)

@Serializable
data class GraphQLListTimelineRoot(
    val data: GraphQLListTimelineData? = null,
)

@Serializable
data class GraphQLListTimelineData(
    val list: GraphQLListTimelineList? = null,
)

@Serializable
data class GraphQLListTimelineList(
    @SerialName("tweets_timeline")
    val tweetsTimeline: GraphQLListTweetsTimeline? = null,
)

@Serializable
data class GraphQLListTweetsTimeline(
    val timeline: Timeline? = null,
)

@Serializable
data class ListEntry(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    @SerialName("member_count")
    val memberCount: Int? = null,
    @SerialName("subscriber_count")
    val subscriberCount: Int? = null,
    val mode: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
)
