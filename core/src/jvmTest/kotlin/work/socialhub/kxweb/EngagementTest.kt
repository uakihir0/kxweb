package work.socialhub.kxweb

import kotlinx.coroutines.test.runTest
import work.socialhub.kxweb.entity.engagement.LikeRequest
import work.socialhub.kxweb.entity.engagement.RetweetRequest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

class EngagementTest {

    @Test
    @Ignore("Integration test - requires auth credentials, modifies data")
    fun testLikeAndUnlike() = runTest {
        val xweb = XWebFactory.instance(
            authToken = System.getenv("XWEB_AUTH_TOKEN") ?: "",
            csrfToken = System.getenv("XWEB_CSRF_TOKEN") ?: "",
        )

        val request = LikeRequest().also {
            it.tweetId = "2019677143589089546"
        }

        val likeResponse = xweb.engagement().like(request)
        println("Like result: ${likeResponse.data.success}")
        assertTrue(likeResponse.data.success)

        val unlikeResponse = xweb.engagement().unlike(request)
        println("Unlike result: ${unlikeResponse.data.success}")
        assertTrue(unlikeResponse.data.success)
    }

    @Test
    @Ignore("Integration test - requires auth credentials, modifies data")
    fun testRetweetAndUnretweet() = runTest {
        val xweb = XWebFactory.instance(
            authToken = System.getenv("XWEB_AUTH_TOKEN") ?: "",
            csrfToken = System.getenv("XWEB_CSRF_TOKEN") ?: "",
        )

        val request = RetweetRequest().also {
            it.tweetId = "2019677143589089546"
        }

        val retweetResponse = xweb.engagement().retweet(request)
        println("Retweet result: ${retweetResponse.data.success}")

        val unretweetResponse = xweb.engagement().unretweet(request)
        println("Unretweet result: ${unretweetResponse.data.success}")
    }
}
