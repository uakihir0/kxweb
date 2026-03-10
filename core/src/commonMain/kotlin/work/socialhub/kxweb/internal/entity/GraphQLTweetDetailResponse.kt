package work.socialhub.kxweb.internal.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GraphQLTweetDetailRoot(
    val data: GraphQLTweetDetailData? = null,
)

@Serializable
data class GraphQLTweetDetailData(
    @SerialName("threaded_conversation_with_injections_v2")
    val threadedConversation: Timeline? = null,
)
