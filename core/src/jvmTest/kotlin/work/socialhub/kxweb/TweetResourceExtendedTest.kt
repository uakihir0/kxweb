package work.socialhub.kxweb

import kotlinx.coroutines.test.runTest
import work.socialhub.kxweb.entity.tweet.GetRepliesRequest
import work.socialhub.kxweb.entity.tweet.GetThreadRequest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TweetResourceExtendedTest {

    @Test
    @Ignore("Integration test - requires auth credentials")
    fun testGetReplies() = runTest {
        val xweb = XWebFactory.instance(
            authToken = System.getenv("XWEB_AUTH_TOKEN") ?: "",
            csrfToken = System.getenv("XWEB_CSRF_TOKEN") ?: "",
        )

        val request = GetRepliesRequest().also {
            it.tweetId = "1234567890"
        }

        val response = xweb.tweet().getReplies(request)

        println("=== Replies ===")
        println("Total: ${response.data.replies.size}")
        println("Cursor: ${response.data.cursor}")

        response.data.replies.forEach { tweet ->
            println("@${tweet.user?.screenName}: ${tweet.text?.take(80)}")
        }
    }

    @Test
    @Ignore("Integration test - requires auth credentials")
    fun testGetThread() = runTest {
        val xweb = XWebFactory.instance(
            authToken = System.getenv("XWEB_AUTH_TOKEN") ?: "",
            csrfToken = System.getenv("XWEB_CSRF_TOKEN") ?: "",
        )

        val request = GetThreadRequest().also {
            it.tweetId = "1234567890"
        }

        val response = xweb.tweet().getThread(request)

        println("=== Thread ===")
        println("Total tweets: ${response.data.tweets.size}")

        response.data.tweets.forEach { tweet ->
            println("${tweet.id}: ${tweet.text?.take(80)}")
        }
    }

    @Test
    fun testGetRepliesRequestFields() {
        val request = GetRepliesRequest()
        request.tweetId = "123"
        request.cursor = "abc"
        assert(request.tweetId == "123")
        assert(request.cursor == "abc")
    }

    @Test
    fun testGetThreadRequestFields() {
        val request = GetThreadRequest()
        request.tweetId = "456"
        request.cursor = "def"
        assert(request.tweetId == "456")
        assert(request.cursor == "def")
    }
}
