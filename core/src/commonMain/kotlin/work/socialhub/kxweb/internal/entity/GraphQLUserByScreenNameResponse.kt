package work.socialhub.kxweb.internal.entity

import kotlinx.serialization.Serializable

@Serializable
data class GraphQLUserByScreenNameRoot(
    val data: GraphQLUserByScreenNameData? = null,
)

@Serializable
data class GraphQLUserByScreenNameData(
    val user: GraphQLUserByScreenNameUser? = null,
)

@Serializable
data class GraphQLUserByScreenNameUser(
    val result: UserResult? = null,
)
