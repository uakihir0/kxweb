package work.socialhub.kxweb.internal.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class GraphQLMutationRoot(
    val data: GraphQLMutationData? = null,
    val errors: List<GraphQLError>? = null,
)

@Serializable
data class GraphQLMutationData(
    @SerialName("favorite_tweet")
    val favoriteTweet: String? = null,
    @SerialName("unfavorite_tweet")
    val unfavoriteTweet: String? = null,
    @SerialName("create_retweet")
    val createRetweet: GraphQLRetweetResult? = null,
    @SerialName("delete_retweet")
    val deleteRetweet: GraphQLRetweetResult? = null,
    @SerialName("create_tweet")
    val createTweet: GraphQLCreateTweetResult? = null,
    @SerialName("delete_tweet")
    val deleteTweet: GraphQLDeleteTweetResult? = null,
    @SerialName("tweet_bookmark_put")
    val tweetBookmarkPut: String? = null,
    @SerialName("tweet_bookmark_delete")
    val tweetBookmarkDelete: String? = null,
)

@Serializable
data class GraphQLRetweetResult(
    @SerialName("retweet_results")
    val retweetResults: GraphQLRetweetResultInner? = null,
)

@Serializable
data class GraphQLRetweetResultInner(
    val result: JsonElement? = null,
)

@Serializable
data class GraphQLCreateTweetResult(
    @SerialName("tweet_results")
    val tweetResults: TweetResults? = null,
)

@Serializable
data class GraphQLDeleteTweetResult(
    @SerialName("tweet_results")
    val tweetResults: JsonElement? = null,
)

@Serializable
data class GraphQLError(
    val message: String? = null,
    val code: Int? = null,
)
