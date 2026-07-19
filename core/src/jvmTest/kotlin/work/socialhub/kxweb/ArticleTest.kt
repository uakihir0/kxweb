package work.socialhub.kxweb

import kotlinx.coroutines.test.runTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Integration test for X (Twitter) Article reading.
 *
 * X Articles are a long-form feature available to a limited set of accounts.
 * Set ARTICLE_TWEET_ID to the ID of a tweet that carries an Article, then
 * run manually. Article content works with guest authentication.
 */
class ArticleTest {

    @Test
    @Ignore("Integration test - requires a known Article tweet ID")
    fun testGetTweetWithArticle() = runTest {
        val tweetId = System.getenv("ARTICLE_TWEET_ID") ?: ""
        val xweb = XWebFactory.instanceGuest()

        val response = xweb.tweet().getTweet(tweetId, withArticle = true)
        val tweet = response.data

        println("=== Article ===")
        println("tweet: ${tweet.id}")
        println("title: ${tweet.article?.title}")
        println("preview: ${tweet.article?.previewText}")
        println("cover: ${tweet.article?.coverImageUrl}")
        println("plainText length: ${tweet.article?.plainText?.length}")

        assertNotNull(tweet.article, "tweet should carry an article")
        assertNotNull(tweet.article?.title)
    }
}
