package work.socialhub.kxweb.internal.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GraphQLViewerRoot(
    val data: GraphQLViewerData? = null,
)

@Serializable
data class GraphQLViewerData(
    val viewer: GraphQLViewer? = null,
)

@Serializable
data class GraphQLViewer(
    @SerialName("user_results")
    val userResults: UserResults? = null,
)
