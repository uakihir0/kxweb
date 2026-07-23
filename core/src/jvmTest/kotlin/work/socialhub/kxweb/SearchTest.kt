package work.socialhub.kxweb

import kotlinx.coroutines.test.runTest
import work.socialhub.kxweb.entity.search.SearchSearchRequest
import work.socialhub.kxweb.entity.search.SearchType
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Integration test for X (Twitter) search API.
 * Set XWEB_RUN_INTEGRATION_TESTS=true and either XWEB_COOKIE or both
 * XWEB_AUTH_TOKEN and XWEB_CSRF_TOKEN to run against the real X API.
 */
class SearchTest {

    @Test
    fun testSearchTweets() = runTest {
        val xweb = authenticatedIntegrationXWeb()

        val request = SearchSearchRequest().also {
            it.query = "Kotlin"
            it.count = 5
            it.searchType = SearchType.LATEST
        }

        val response = xweb.search().searchTweets(request)

        println("=== Search Results ===")
        println("Total tweets: ${response.data.tweets.size}")
        println("Cursor: ${response.data.cursor}")

        response.data.tweets.forEach { tweet ->
            println("---")
            println("ID: ${tweet.id}")
            println("User: @${tweet.user?.screenName}")
            println("Text: ${tweet.text?.take(100)}")
            println("Likes: ${tweet.favoriteCount}, RT: ${tweet.retweetCount}")
        }

        assertTrue(response.data.tweets.isNotEmpty(), "Search should return results")
    }

    @Test
    fun testSearchTweetsBlocking() {
        val xweb = authenticatedIntegrationXWeb()

        val request = SearchSearchRequest().also {
            it.query = "Kotlin Multiplatform"
            it.count = 3
            it.searchType = SearchType.TOP
        }

        val response = xweb.search().searchTweetsBlocking(request)

        println("=== Blocking Search Results ===")
        println("Total tweets: ${response.data.tweets.size}")

        response.data.tweets.forEach { tweet ->
            println("@${tweet.user?.screenName}: ${tweet.text?.take(80)}")
        }

        assertTrue(response.data.tweets.isNotEmpty(), "Search should return results")
    }

    @Test
    fun testSearchWithPagination() = runTest {
        val xweb = authenticatedIntegrationXWeb()

        // First page
        val request1 = SearchSearchRequest().also {
            it.query = "Kotlin"
            it.count = 3
        }
        val response1 = xweb.search().searchTweets(request1)
        println("Page 1: ${response1.data.tweets.size} tweets")
        println("Next cursor: ${response1.data.cursor}")

        // Second page
        if (response1.data.cursor != null) {
            val request2 = SearchSearchRequest().also {
                it.query = "Kotlin"
                it.count = 3
                it.cursor = response1.data.cursor
            }
            val response2 = xweb.search().searchTweets(request2)
            println("Page 2: ${response2.data.tweets.size} tweets")

            assertTrue(response2.data.tweets.isNotEmpty(), "Second page should return results")
        }
    }

}
