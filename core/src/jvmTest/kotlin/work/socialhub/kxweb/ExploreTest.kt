package work.socialhub.kxweb

import kotlinx.coroutines.test.runTest
import work.socialhub.kxweb.entity.explore.GetNewsRequest
import kotlin.test.Ignore
import kotlin.test.Test

class ExploreTest {

    @Test
    @Ignore("Integration test - requires auth credentials")
    fun testGetNews() = runTest {
        val xweb = XWebFactory.instance(
            authToken = System.getenv("XWEB_AUTH_TOKEN") ?: "",
            csrfToken = System.getenv("XWEB_CSRF_TOKEN") ?: "",
        )

        val request = GetNewsRequest().also {
            it.count = 10
        }

        val response = xweb.explore().getNews(request)

        println("=== Explore / News ===")
        println("Total: ${response.data.tweets.size}")

        response.data.tweets.forEach { tweet ->
            println("@${tweet.user?.screenName}: ${tweet.text?.take(80)}")
        }
    }
}
