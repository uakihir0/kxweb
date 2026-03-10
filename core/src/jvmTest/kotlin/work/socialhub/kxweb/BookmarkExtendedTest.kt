package work.socialhub.kxweb

import kotlinx.coroutines.test.runTest
import work.socialhub.kxweb.entity.bookmark.SearchBookmarksRequest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertNotNull

class BookmarkExtendedTest {

    @Test
    fun testSearchBookmarksRequestFields() {
        val request = SearchBookmarksRequest()
        request.query = "kotlin"
        request.count = 10
        request.cursor = "abc"
        assert(request.query == "kotlin")
        assert(request.count == 10)
        assert(request.cursor == "abc")
    }

    @Test
    @Ignore("Integration test - requires auth credentials")
    fun testSearchBookmarks() = runTest {
        val xweb = XWebFactory.instance(
            authToken = System.getenv("XWEB_AUTH_TOKEN") ?: "",
            csrfToken = System.getenv("XWEB_CSRF_TOKEN") ?: "",
        )

        val request = SearchBookmarksRequest().also {
            it.query = "kotlin"
            it.count = 5
        }

        val response = xweb.bookmark().searchBookmarks(request)

        println("=== Bookmark Search ===")
        println("Total: ${response.data.tweets.size}")

        response.data.tweets.forEach { tweet ->
            println("@${tweet.user?.screenName}: ${tweet.text?.take(80)}")
        }
    }
}
