package work.socialhub.kxweb

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertEquals

class TweetResultByRestIdTest {

    @Test
    fun testGetTweetById() = runTest {
        // No auth required — uses public Bearer token only
        val xweb = XWebFactory.instance()

        val tweetId = "2019677143589089546"
        val response = xweb.tweet().getTweet(tweetId)
        val tweet = response.data

        println("=== TweetResultByRestId ===")
        println("ID: ${tweet.id}")
        println("ユーザー: @${tweet.user?.screenName} (${tweet.user?.name})")
        println("テキスト: ${tweet.text?.take(200)}")
        println("いいね: ${tweet.favoriteCount}, RT: ${tweet.retweetCount}, リプ: ${tweet.replyCount}")
        println("表示回数: ${tweet.viewCount}")
        println("投稿日時: ${tweet.createdAt}")
        println("メディア: ${tweet.media.size}件")
        tweet.media.forEach { media ->
            println("  - ${media.type}: ${media.url}")
        }

        assertNotNull(tweet.id)
        assertEquals(tweetId, tweet.id)
        assertNotNull(tweet.text)
        assertNotNull(tweet.user)
        assertEquals("AUTOMATONJapan", tweet.user?.screenName)
    }
}
