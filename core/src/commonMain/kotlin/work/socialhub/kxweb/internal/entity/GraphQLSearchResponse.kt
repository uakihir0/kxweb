package work.socialhub.kxweb.internal.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Internal models for deserializing the deeply nested
 * X (Twitter) GraphQL search response.
 *
 * Response path:
 *   data.search_by_raw_query.search_timeline.timeline.instructions[].entries[]
 *     .content.itemContent.tweet_results.result
 *       .rest_id
 *       .legacy (full_text, created_at, metrics...)
 *       .core.user_results.result.legacy (screen_name, name...)
 */

@Serializable
data class GraphQLSearchRoot(
    val data: GraphQLSearchData? = null,
)

@Serializable
data class GraphQLSearchData(
    @SerialName("search_by_raw_query")
    val searchByRawQuery: SearchByRawQuery? = null,
)

@Serializable
data class SearchByRawQuery(
    @SerialName("search_timeline")
    val searchTimeline: SearchTimeline? = null,
)

@Serializable
data class SearchTimeline(
    val timeline: Timeline? = null,
)

@Serializable
data class Timeline(
    val instructions: List<TimelineInstruction> = emptyList(),
)

@Serializable
data class TimelineInstruction(
    val type: String? = null,
    val entries: List<TimelineEntry>? = null,
)

@Serializable
data class TimelineEntry(
    @SerialName("entryId")
    val entryId: String? = null,
    val sortIndex: String? = null,
    val content: TimelineEntryContent? = null,
)

@Serializable
data class TimelineEntryContent(
    @SerialName("entryType")
    val entryType: String? = null,
    val itemContent: ItemContent? = null,
    // For module entries (e.g. user search results grouped in a module)
    val items: List<TimelineModuleItem>? = null,
    // For cursor entries
    val value: String? = null,
    val cursorType: String? = null,
)

@Serializable
data class TimelineModuleItem(
    @SerialName("entryId")
    val entryId: String? = null,
    val item: TimelineModuleItemInner? = null,
)

@Serializable
data class TimelineModuleItemInner(
    val itemContent: ItemContent? = null,
)

@Serializable
data class ItemContent(
    @SerialName("itemType")
    val itemType: String? = null,
    @SerialName("tweet_results")
    val tweetResults: TweetResults? = null,
    @SerialName("user_results")
    val userResults: UserResults? = null,
)

@Serializable
data class TweetResults(
    val result: TweetResult? = null,
)

@Serializable
data class TweetResult(
    @SerialName("__typename")
    val typename: String? = null,
    @SerialName("rest_id")
    val restId: String? = null,
    val core: TweetCore? = null,
    val legacy: TweetLegacy? = null,
    val views: TweetViews? = null,
    val article: ArticleContainer? = null,
    // Catch additional fields without failing
    @SerialName("unmention_data")
    val unmentionData: JsonElement? = null,
)

/**
 * Article embedded in a tweet result. Present when article field toggles are
 * enabled. Field names are best-effort against the undocumented schema; unknown
 * keys are ignored so missing fields simply deserialize to null.
 */
@Serializable
data class ArticleContainer(
    @SerialName("article_results")
    val articleResults: ArticleResults? = null,
)

@Serializable
data class ArticleResults(
    val result: ArticleResult? = null,
)

@Serializable
data class ArticleResult(
    @SerialName("rest_id")
    val restId: String? = null,
    val title: String? = null,
    @SerialName("preview_text")
    val previewText: String? = null,
    @SerialName("plain_text")
    val plainText: String? = null,
    @SerialName("cover_media")
    val coverMedia: ArticleCoverMedia? = null,
)

@Serializable
data class ArticleCoverMedia(
    @SerialName("media_info")
    val mediaInfo: ArticleCoverMediaInfo? = null,
)

@Serializable
data class ArticleCoverMediaInfo(
    @SerialName("original_img_url")
    val originalImgUrl: String? = null,
)

@Serializable
data class TweetViews(
    val count: String? = null,
    val state: String? = null,
)

@Serializable
data class TweetCore(
    @SerialName("user_results")
    val userResults: UserResults? = null,
)

@Serializable
data class UserResults(
    val result: UserResult? = null,
)

@Serializable
data class UserResult(
    @SerialName("__typename")
    val typename: String? = null,
    @SerialName("rest_id")
    val restId: String? = null,
    /** User core info (name, screen_name) — used by TweetResultByRestId */
    val core: UserResultCore? = null,
    val legacy: UserLegacy? = null,
    @SerialName("is_blue_verified")
    val isBlueVerified: Boolean? = null,
)

@Serializable
data class UserResultCore(
    val name: String? = null,
    @SerialName("screen_name")
    val screenName: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
)

@Serializable
data class UserLegacy(
    @SerialName("screen_name")
    val screenName: String? = null,
    val name: String? = null,
    val description: String? = null,
    @SerialName("profile_image_url_https")
    val profileImageUrlHttps: String? = null,
    @SerialName("profile_banner_url")
    val profileBannerUrl: String? = null,
    @SerialName("followers_count")
    val followersCount: Int? = null,
    @SerialName("friends_count")
    val friendsCount: Int? = null,
    @SerialName("statuses_count")
    val statusesCount: Int? = null,
    @SerialName("listed_count")
    val listedCount: Int? = null,
    val verified: Boolean? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    val location: String? = null,
    val url: String? = null,
)

@Serializable
data class TweetLegacy(
    @SerialName("full_text")
    val fullText: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("reply_count")
    val replyCount: Int? = null,
    @SerialName("retweet_count")
    val retweetCount: Int? = null,
    @SerialName("favorite_count")
    val favoriteCount: Int? = null,
    @SerialName("bookmark_count")
    val bookmarkCount: Int? = null,
    @SerialName("quote_count")
    val quoteCount: Int? = null,
    @SerialName("in_reply_to_status_id_str")
    val inReplyToStatusIdStr: String? = null,
    @SerialName("conversation_id_str")
    val conversationIdStr: String? = null,
    val lang: String? = null,
    @SerialName("extended_entities")
    val extendedEntities: ExtendedEntities? = null,
    val entities: TweetEntities? = null,
)

@Serializable
data class ExtendedEntities(
    val media: List<MediaEntity> = emptyList(),
)

@Serializable
data class TweetEntities(
    val media: List<MediaEntity>? = null,
)

@Serializable
data class MediaEntity(
    val type: String? = null,
    @SerialName("media_url_https")
    val mediaUrlHttps: String? = null,
    @SerialName("original_info")
    val originalInfo: MediaOriginalInfo? = null,
)

@Serializable
data class MediaOriginalInfo(
    val width: Int? = null,
    val height: Int? = null,
)
