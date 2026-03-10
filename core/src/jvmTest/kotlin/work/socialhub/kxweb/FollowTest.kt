package work.socialhub.kxweb

import kotlinx.coroutines.test.runTest
import work.socialhub.kxweb.entity.follow.FollowRequest
import kotlin.test.Ignore
import kotlin.test.Test

class FollowTest {

    @Test
    @Ignore("Integration test - requires auth credentials, modifies relationships")
    fun testFollowAndUnfollow() = runTest {
        val xweb = XWebFactory.instance(
            authToken = System.getenv("XWEB_AUTH_TOKEN") ?: "",
            csrfToken = System.getenv("XWEB_CSRF_TOKEN") ?: "",
        )

        val request = FollowRequest().also {
            it.userId = "44196397" // @elonmusk
        }

        val followResponse = xweb.follow().follow(request)
        println("Follow: ${followResponse.data.success}")

        val unfollowResponse = xweb.follow().unfollow(request)
        println("Unfollow: ${unfollowResponse.data.success}")
    }
}
