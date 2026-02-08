package work.socialhub.kxweb.internal.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Internal models for deserializing the X (Twitter) GraphQL
 * TweetResultByRestId response.
 *
 * Response path:
 *   data.tweetResult.result
 *     .rest_id
 *     .legacy (full_text, created_at, metrics...)
 *     .core.user_results.result.legacy (screen_name, name...)
 *     .views.count
 */

@Serializable
data class GraphQLTweetRoot(
    val data: GraphQLTweetData? = null,
)

@Serializable
data class GraphQLTweetData(
    val tweetResult: TweetResults? = null,
)
