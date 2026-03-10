package work.socialhub.kxweb

import kotlinx.coroutines.test.runTest
import work.socialhub.kxweb.entity.timeline.GetLikesRequest
import kotlin.test.Ignore
import kotlin.test.Test

class TimelineTest {

    @Test
    @Ignore("Integration test - requires auth credentials")
    fun testGetLikes() = runTest {
        val xweb = XWebFactory.instance(
            authToken = System.getenv("XWEB_AUTH_TOKEN") ?: "",
            csrfToken = System.getenv("XWEB_CSRF_TOKEN") ?: "",
        )

        val request = GetLikesRequest().also {
            it.userId = "44196397"
            it.count = 5
        }

        val response = xweb.timeline().getLikes(request)

        println("=== Likes ===")
        println("Total: ${response.data.tweets.size}")

        response.data.tweets.forEach { tweet ->
            println("@${tweet.user?.screenName}: ${tweet.text?.take(80)}")
        }
    }
}
