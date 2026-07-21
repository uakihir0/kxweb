package work.socialhub.kxweb

import kotlinx.serialization.json.Json
import work.socialhub.kxweb.internal.entity.ArticleContainer
import work.socialhub.kxweb.internal.entity.ArticleCoverMedia
import work.socialhub.kxweb.internal.entity.ArticleCoverMediaInfo
import work.socialhub.kxweb.internal.entity.ArticleResult
import work.socialhub.kxweb.internal.entity.ArticleResults
import work.socialhub.kxweb.internal.entity.ItemContent
import work.socialhub.kxweb.internal.entity.TimelineEntry
import work.socialhub.kxweb.internal.entity.TimelineEntryContent
import work.socialhub.kxweb.internal.entity.TimelineInstruction
import work.socialhub.kxweb.internal.entity.TimelineModuleItem
import work.socialhub.kxweb.internal.entity.TimelineModuleItemInner
import work.socialhub.kxweb.internal.entity.TweetCore
import work.socialhub.kxweb.internal.entity.TweetLegacy
import work.socialhub.kxweb.internal.entity.TweetResult
import work.socialhub.kxweb.internal.entity.TweetResults
import work.socialhub.kxweb.internal.entity.TweetViews
import work.socialhub.kxweb.internal.entity.UserLegacy
import work.socialhub.kxweb.internal.entity.UserResult
import work.socialhub.kxweb.internal.entity.UserResultCore
import work.socialhub.kxweb.internal.entity.UserResults
import work.socialhub.kxweb.internal.share.TweetParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TweetParserTest {

    @Test
    fun testParseTweetResult() {
        val tweetResult = TweetResult(
            restId = "123456",
            legacy = TweetLegacy(
                fullText = "Hello World",
                createdAt = "Mon Jan 01 00:00:00 +0000 2024",
                replyCount = 5,
                retweetCount = 10,
                favoriteCount = 100,
                bookmarkCount = 3,
                quoteCount = 2,
                conversationIdStr = "123456",
                lang = "en",
            ),
            core = TweetCore(
                userResults = UserResults(
                    result = UserResult(
                        restId = "789",
                        legacy = UserLegacy(
                            screenName = "testuser",
                            name = "Test User",
                            description = "A test user",
                            followersCount = 1000,
                            friendsCount = 500,
                            statusesCount = 2000,
                        ),
                        isBlueVerified = true,
                    )
                )
            ),
            views = TweetViews(count = "5000"),
        )

        val tweet = TweetParser.parseTweetResult(tweetResult)

        assertEquals("123456", tweet.id)
        assertEquals("Hello World", tweet.text)
        assertEquals(5, tweet.replyCount)
        assertEquals(10, tweet.retweetCount)
        assertEquals(100, tweet.favoriteCount)
        assertEquals(3, tweet.bookmarkCount)
        assertEquals(2, tweet.quoteCount)
        assertEquals(5000L, tweet.viewCount)
        assertEquals("en", tweet.lang)

        assertNotNull(tweet.user)
        assertEquals("789", tweet.user?.id)
        assertEquals("testuser", tweet.user?.screenName)
        assertEquals("Test User", tweet.user?.name)
        assertEquals(true, tweet.user?.verified)
        assertEquals(1000, tweet.user?.followersCount)
        assertEquals(500, tweet.user?.followingCount)
        assertEquals(2000, tweet.user?.statusesCount)
    }

    @Test
    fun testParseRetweetedTweet() {
        val tweetResult = TweetResult(
            restId = "retweet-1",
            legacy = TweetLegacy(
                fullText = "RT @original: Original post",
                retweetedStatusResult = TweetResults(
                    result = TweetResult(
                        restId = "original-1",
                        legacy = TweetLegacy(
                            fullText = "Original post",
                            favoriteCount = 12,
                        ),
                        core = TweetCore(
                            userResults = UserResults(
                                result = UserResult(
                                    restId = "original-user",
                                    core = UserResultCore(
                                        name = "Original User",
                                        screenName = "original",
                                    ),
                                )
                            )
                        ),
                    )
                ),
            ),
            core = TweetCore(
                userResults = UserResults(
                    result = UserResult(
                        restId = "retweet-user",
                        core = UserResultCore(
                            name = "Retweet User",
                            screenName = "retweeter",
                        ),
                    )
                )
            ),
        )

        val tweet = TweetParser.parseTweetResult(tweetResult)

        assertEquals("retweet-user", tweet.user?.id)
        assertEquals("original-1", tweet.retweetedTweet?.id)
        assertEquals("Original post", tweet.retweetedTweet?.text)
        assertEquals("original-user", tweet.retweetedTweet?.user?.id)
        assertEquals(12, tweet.retweetedTweet?.favoriteCount)
    }

    @Test
    fun testDeserializeRetweetedStatusResult() {
        val tweetResult = Json { ignoreUnknownKeys = true }.decodeFromString<TweetResult>(
            """
            {
              "rest_id": "retweet-1",
              "legacy": {
                "full_text": "RT @original: Original post",
                "retweeted_status_result": {
                  "result": {
                    "rest_id": "original-1",
                    "legacy": {
                      "full_text": "Original post"
                    }
                  }
                }
              }
            }
            """.trimIndent()
        )

        val tweet = TweetParser.parseTweetResult(tweetResult)

        assertEquals("original-1", tweet.retweetedTweet?.id)
        assertEquals("Original post", tweet.retweetedTweet?.text)
    }

    @Test
    fun testDeserializeVisibilityWrappedRetweetedStatusResult() {
        val tweetResult = Json { ignoreUnknownKeys = true }.decodeFromString<TweetResult>(
            """
            {
              "rest_id": "retweet-1",
              "legacy": {
                "full_text": "RT @original: Original post",
                "retweeted_status_result": {
                  "result": {
                    "__typename": "TweetWithVisibilityResults",
                    "tweet": {
                      "__typename": "Tweet",
                      "rest_id": "original-1",
                      "legacy": {
                        "full_text": "Original post",
                        "favorite_count": 12
                      }
                    }
                  }
                }
              }
            }
            """.trimIndent()
        )

        val tweet = TweetParser.parseTweetResult(tweetResult)

        assertEquals("original-1", tweet.retweetedTweet?.id)
        assertEquals("Original post", tweet.retweetedTweet?.text)
        assertEquals(12, tweet.retweetedTweet?.favoriteCount)
    }

    @Test
    fun testIgnoreUnavailableRetweetedStatusResult() {
        val tweetResult = TweetResult(
            restId = "retweet-1",
            legacy = TweetLegacy(
                fullText = "RT @unavailable: Unavailable post",
                retweetedStatusResult = TweetResults(
                    result = TweetResult(typename = "TweetUnavailable")
                ),
            ),
        )

        val tweet = TweetParser.parseTweetResult(tweetResult)

        assertNull(tweet.retweetedTweet)
        assertEquals("RT @unavailable: Unavailable post", tweet.text)
    }

    @Test
    fun testParseUserResult() {
        val userResult = UserResult(
            restId = "456",
            core = UserResultCore(
                name = "Core Name",
                screenName = "coreuser",
                createdAt = "Tue Mar 01 00:00:00 +0000 2010",
            ),
            legacy = UserLegacy(
                description = "Bio text",
                profileImageUrlHttps = "https://example.com/img.jpg",
                profileBannerUrl = "https://example.com/banner.jpg",
                followersCount = 2000,
                friendsCount = 100,
                statusesCount = 5000,
                listedCount = 50,
                location = "Tokyo",
                url = "https://example.com",
            ),
            isBlueVerified = false,
        )

        val user = TweetParser.parseUserResult(userResult)

        assertEquals("456", user.id)
        assertEquals("coreuser", user.screenName)
        assertEquals("Core Name", user.name)
        assertEquals("Bio text", user.description)
        assertEquals("https://example.com/img.jpg", user.profileImageUrl)
        assertEquals("https://example.com/banner.jpg", user.profileBannerUrl)
        assertEquals(2000, user.followersCount)
        assertEquals(100, user.followingCount)
        assertEquals(5000, user.statusesCount)
        assertEquals(50, user.listedCount)
        assertEquals(false, user.verified)
        assertEquals("Tue Mar 01 00:00:00 +0000 2010", user.createdAt)
        assertEquals("Tokyo", user.location)
        assertEquals("https://example.com", user.url)
    }

    @Test
    fun testParseTimelineInstructions() {
        val instructions = listOf(
            TimelineInstruction(
                type = "TimelineAddEntries",
                entries = listOf(
                    TimelineEntry(
                        entryId = "tweet-1",
                        content = TimelineEntryContent(
                            itemContent = ItemContent(
                                tweetResults = TweetResults(
                                    result = TweetResult(
                                        restId = "1",
                                        legacy = TweetLegacy(fullText = "Tweet 1"),
                                    )
                                )
                            )
                        )
                    ),
                    TimelineEntry(
                        entryId = "tweet-2",
                        content = TimelineEntryContent(
                            itemContent = ItemContent(
                                tweetResults = TweetResults(
                                    result = TweetResult(
                                        restId = "2",
                                        legacy = TweetLegacy(fullText = "Tweet 2"),
                                    )
                                )
                            )
                        )
                    ),
                    TimelineEntry(
                        entryId = "cursor-bottom",
                        content = TimelineEntryContent(
                            cursorType = "Bottom",
                            value = "next_cursor_value",
                        )
                    ),
                )
            )
        )

        val result = TweetParser.parseTimelineInstructions(instructions)

        assertEquals(2, result.tweets.size)
        assertEquals("1", result.tweets[0].id)
        assertEquals("Tweet 1", result.tweets[0].text)
        assertEquals("2", result.tweets[1].id)
        assertEquals("Tweet 2", result.tweets[1].text)
        assertEquals("next_cursor_value", result.cursor)
    }

    @Test
    fun testParseEmptyInstructions() {
        val result = TweetParser.parseTimelineInstructions(emptyList())

        assertEquals(0, result.tweets.size)
        assertNull(result.cursor)
    }

    @Test
    fun testParseTweetResultWithArticle() {
        val tweetResult = TweetResult(
            restId = "999",
            legacy = TweetLegacy(fullText = "See my article"),
            article = ArticleContainer(
                articleResults = ArticleResults(
                    result = ArticleResult(
                        restId = "art-1",
                        title = "How X Works",
                        previewText = "A short preview",
                        plainText = "The full article body text.",
                        coverMedia = ArticleCoverMedia(
                            mediaInfo = ArticleCoverMediaInfo(
                                originalImgUrl = "https://example.com/cover.jpg",
                            )
                        ),
                    )
                )
            ),
        )

        val tweet = TweetParser.parseTweetResult(tweetResult)

        assertNotNull(tweet.article)
        assertEquals("art-1", tweet.article?.id)
        assertEquals("How X Works", tweet.article?.title)
        assertEquals("A short preview", tweet.article?.previewText)
        assertEquals("The full article body text.", tweet.article?.plainText)
        assertEquals("https://example.com/cover.jpg", tweet.article?.coverImageUrl)
    }

    @Test
    fun testParseTweetResultWithoutArticle() {
        val tweetResult = TweetResult(
            restId = "1000",
            legacy = TweetLegacy(fullText = "Plain tweet"),
        )

        val tweet = TweetParser.parseTweetResult(tweetResult)
        assertNull(tweet.article)
    }

    @Test
    fun testParseUsersFromModuleItems() {
        val instructions = listOf(
            TimelineInstruction(
                type = "TimelineAddEntries",
                entries = listOf(
                    TimelineEntry(
                        entryId = "user-module",
                        content = TimelineEntryContent(
                            items = listOf(
                                TimelineModuleItem(
                                    entryId = "user-module-0",
                                    item = TimelineModuleItemInner(
                                        itemContent = ItemContent(
                                            userResults = UserResults(
                                                result = UserResult(
                                                    restId = "11",
                                                    legacy = UserLegacy(screenName = "alice"),
                                                )
                                            )
                                        )
                                    )
                                ),
                                TimelineModuleItem(
                                    entryId = "user-module-1",
                                    item = TimelineModuleItemInner(
                                        itemContent = ItemContent(
                                            userResults = UserResults(
                                                result = UserResult(
                                                    restId = "12",
                                                    legacy = UserLegacy(screenName = "bob"),
                                                )
                                            )
                                        )
                                    )
                                ),
                            )
                        )
                    ),
                    TimelineEntry(
                        entryId = "cursor-bottom",
                        content = TimelineEntryContent(
                            cursorType = "Bottom",
                            value = "cursor_x",
                        )
                    ),
                )
            )
        )

        val result = TweetParser.parseUserTimelineInstructions(instructions)

        assertEquals(2, result.users.size)
        assertEquals("alice", result.users[0].screenName)
        assertEquals("bob", result.users[1].screenName)
        assertEquals("cursor_x", result.cursor)
    }
}
