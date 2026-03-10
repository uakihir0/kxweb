package work.socialhub.kxweb

import kotlinx.coroutines.test.runTest
import work.socialhub.kxweb.entity.bookmark.BookmarkRequest
import work.socialhub.kxweb.entity.bookmark.GetBookmarksRequest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

class BookmarkTest {

    @Test
    @Ignore("Integration test - requires auth credentials")
    fun testGetBookmarks() = runTest {
        val xweb = XWebFactory.instance(
            authToken = System.getenv("XWEB_AUTH_TOKEN") ?: "",
            csrfToken = System.getenv("XWEB_CSRF_TOKEN") ?: "",
        )

        val request = GetBookmarksRequest().also {
            it.count = 5
        }

        val response = xweb.bookmark().getBookmarks(request)

        println("=== Bookmarks ===")
        println("Total: ${response.data.tweets.size}")

        response.data.tweets.forEach { tweet ->
            println("@${tweet.user?.screenName}: ${tweet.text?.take(80)}")
        }
    }

    @Test
    @Ignore("Integration test - requires auth credentials, modifies data")
    fun testBookmarkAndUnbookmark() = runTest {
        val xweb = XWebFactory.instance(
            authToken = System.getenv("XWEB_AUTH_TOKEN") ?: "",
            csrfToken = System.getenv("XWEB_CSRF_TOKEN") ?: "",
        )

        val request = BookmarkRequest().also {
            it.tweetId = "2019677143589089546"
        }

        val bookmarkResponse = xweb.bookmark().bookmark(request)
        println("Bookmark: ${bookmarkResponse.data.success}")
        assertTrue(bookmarkResponse.data.success)

        val unbookmarkResponse = xweb.bookmark().unbookmark(request)
        println("Unbookmark: ${unbookmarkResponse.data.success}")
        assertTrue(unbookmarkResponse.data.success)
    }
}
