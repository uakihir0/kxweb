package work.socialhub.kxweb

import kotlinx.coroutines.test.runTest
import work.socialhub.kxweb.entity.search.SearchUsersRequest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Integration test for X (Twitter) user search.
 *
 * User search requires a logged-in session (guest access returns 404).
 * Set XWEB_RUN_INTEGRATION_TESTS=true and either XWEB_COOKIE or both
 * XWEB_AUTH_TOKEN and XWEB_CSRF_TOKEN from a browser session.
 */
class SearchUsersTest {

    @Test
    fun testSearchUsers() = runTest {
        val xweb = authenticatedIntegrationXWeb()

        val request = SearchUsersRequest().also {
            it.query = "Kotlin"
            it.count = 10
        }

        val response = xweb.search().searchUsers(request)
        val users = response.data.users

        println("=== User Search Results ===")
        println("count: ${users.size}")
        println("cursor: ${response.data.cursor}")
        users.forEach { user ->
            println("  - @${user.screenName} (${user.name}) followers=${user.followersCount}")
        }

        assertTrue(users.isNotEmpty(), "User search should return results")
    }
}
