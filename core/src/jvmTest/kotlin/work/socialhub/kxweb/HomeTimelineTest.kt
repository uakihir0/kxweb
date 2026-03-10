package work.socialhub.kxweb

import kotlinx.coroutines.test.runTest
import work.socialhub.kxweb.entity.home.HomeTimelineRequest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

class HomeTimelineTest {

    @Test
    @Ignore("Integration test - requires auth credentials")
    fun testGetHomeTimeline() = runTest {
        val xweb = XWebFactory.instance(
            authToken = System.getenv("XWEB_AUTH_TOKEN") ?: "",
            csrfToken = System.getenv("XWEB_CSRF_TOKEN") ?: "",
        )

        val request = HomeTimelineRequest().also {
            it.count = 5
        }

        val response = xweb.home().getHomeTimeline(request)

        println("=== Home Timeline (For You) ===")
        println("Total tweets: ${response.data.tweets.size}")
        println("Cursor: ${response.data.cursor}")

        response.data.tweets.forEach { tweet ->
            println("@${tweet.user?.screenName}: ${tweet.text?.take(80)}")
        }

        assertTrue(response.data.tweets.isNotEmpty())
    }

    @Test
    @Ignore("Integration test - requires auth credentials")
    fun testGetHomeLatestTimeline() = runTest {
        val xweb = XWebFactory.instance(
            authToken = System.getenv("XWEB_AUTH_TOKEN") ?: "",
            csrfToken = System.getenv("XWEB_CSRF_TOKEN") ?: "",
        )

        val request = HomeTimelineRequest().also {
            it.count = 5
        }

        val response = xweb.home().getHomeLatestTimeline(request)

        println("=== Home Timeline (Following) ===")
        println("Total tweets: ${response.data.tweets.size}")

        response.data.tweets.forEach { tweet ->
            println("@${tweet.user?.screenName}: ${tweet.text?.take(80)}")
        }

        assertTrue(response.data.tweets.isNotEmpty())
    }
}
