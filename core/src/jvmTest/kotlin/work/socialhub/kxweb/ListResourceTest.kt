package work.socialhub.kxweb

import kotlinx.coroutines.test.runTest
import work.socialhub.kxweb.entity.list.GetListsRequest
import work.socialhub.kxweb.entity.list.ListTimelineRequest
import kotlin.test.Ignore
import kotlin.test.Test

class ListResourceTest {

    @Test
    @Ignore("Integration test - requires auth credentials")
    fun testGetOwnedLists() = runTest {
        val xweb = XWebFactory.instance(
            authToken = System.getenv("XWEB_AUTH_TOKEN") ?: "",
            csrfToken = System.getenv("XWEB_CSRF_TOKEN") ?: "",
        )

        val request = GetListsRequest().also {
            it.userId = "44196397"
            it.count = 5
        }

        val response = xweb.list().getOwnedLists(request)

        println("=== Owned Lists ===")
        println("Total tweets: ${response.data.tweets.size}")
    }

    @Test
    @Ignore("Integration test - requires auth credentials and valid list ID")
    fun testGetListTimeline() = runTest {
        val xweb = XWebFactory.instance(
            authToken = System.getenv("XWEB_AUTH_TOKEN") ?: "",
            csrfToken = System.getenv("XWEB_CSRF_TOKEN") ?: "",
        )

        val request = ListTimelineRequest().also {
            it.listId = "1234567890" // Replace with real list ID
            it.count = 5
        }

        val response = xweb.list().getListTimeline(request)

        println("=== List Timeline ===")
        println("Total: ${response.data.tweets.size}")

        response.data.tweets.forEach { tweet ->
            println("@${tweet.user?.screenName}: ${tweet.text?.take(80)}")
        }
    }
}
