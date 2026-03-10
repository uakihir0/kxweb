package work.socialhub.kxweb

import kotlinx.coroutines.test.runTest
import work.socialhub.kxweb.entity.tweet.TweetDetailRequest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

class TweetDetailTest {

    @Test
    @Ignore("Integration test - requires auth credentials")
    fun testGetTweetDetail() = runTest {
        val xweb = XWebFactory.instance(
            authToken = System.getenv("XWEB_AUTH_TOKEN") ?: "",
            csrfToken = System.getenv("XWEB_CSRF_TOKEN") ?: "",
        )

        val request = TweetDetailRequest().also {
            it.tweetId = "2019677143589089546"
        }

        val response = xweb.tweet().getTweetDetail(request)

        println("=== Tweet Detail ===")
        println("Total tweets in thread: ${response.data.tweets.size}")
        println("Cursor: ${response.data.cursor}")

        response.data.tweets.forEach { tweet ->
            println("@${tweet.user?.screenName}: ${tweet.text?.take(80)}")
        }

        assertTrue(response.data.tweets.isNotEmpty())
    }
}
