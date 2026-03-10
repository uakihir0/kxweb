package work.socialhub.kxweb.internal.entity

import kotlinx.serialization.Serializable

@Serializable
data class GraphQLAboutAccountRoot(
    val data: GraphQLAboutAccountData? = null,
)

@Serializable
data class GraphQLAboutAccountData(
    val user_result_by_screen_name: GraphQLAboutAccountUserResult? = null,
)

@Serializable
data class GraphQLAboutAccountUserResult(
    val result: GraphQLAboutAccountResult? = null,
)

@Serializable
data class GraphQLAboutAccountResult(
    val about_profile: GraphQLAboutProfile? = null,
)

@Serializable
data class GraphQLAboutProfile(
    val id: String? = null,
    val created_at: String? = null,
    val location: String? = null,
    val description: String? = null,
)
