package work.socialhub.kxweb.internal.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GraphQLBookmarksRoot(
    val data: GraphQLBookmarksData? = null,
)

@Serializable
data class GraphQLBookmarksData(
    @SerialName("bookmark_timeline_v2")
    val bookmarkTimeline: GraphQLBookmarkTimeline? = null,
)

@Serializable
data class GraphQLBookmarkTimeline(
    val timeline: Timeline? = null,
)
