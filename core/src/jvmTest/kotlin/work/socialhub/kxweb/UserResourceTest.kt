package work.socialhub.kxweb

import kotlinx.coroutines.test.runTest
import work.socialhub.kxweb.entity.user.FollowingRequest
import work.socialhub.kxweb.entity.user.UserByScreenNameRequest
import work.socialhub.kxweb.entity.user.UserTweetsRequest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UserResourceTest {

    @Test
    @Ignore("Integration test - requires auth credentials")
    fun testGetUserByScreenName() = runTest {
        val xweb = XWebFactory.instance(
            authToken = System.getenv("XWEB_AUTH_TOKEN") ?: "",
            csrfToken = System.getenv("XWEB_CSRF_TOKEN") ?: "",
        )

        val request = UserByScreenNameRequest().also {
            it.screenName = "elikiAr0"
        }

        val response = xweb.user().getUserByScreenName(request)
        val user = response.data

        println("=== User By Screen Name ===")
        println("ID: ${user.id}")
        println("Name: ${user.name}")
        println("Screen Name: @${user.screenName}")
        println("Description: ${user.description?.take(100)}")
        println("Followers: ${user.followersCount}")
        println("Following: ${user.followingCount}")
        println("Tweets: ${user.statusesCount}")
        println("Verified: ${user.verified}")

        assertNotNull(user.id)
        assertNotNull(user.screenName)
    }

    @Test
    @Ignore("Integration test - requires auth credentials")
    fun testGetUserTweets() = runTest {
        val xweb = XWebFactory.instance(
            authToken = System.getenv("XWEB_AUTH_TOKEN") ?: "",
            csrfToken = System.getenv("XWEB_CSRF_TOKEN") ?: "",
        )

        val request = UserTweetsRequest().also {
            it.userId = "44196397" // @elonmusk
            it.count = 5
        }

        val response = xweb.user().getUserTweets(request)

        println("=== User Tweets ===")
        println("Total: ${response.data.tweets.size}")
        println("Cursor: ${response.data.cursor}")

        response.data.tweets.forEach { tweet ->
            println("${tweet.text?.take(80)}")
        }

        assertTrue(response.data.tweets.isNotEmpty())
    }

    @Test
    @Ignore("Integration test - requires auth credentials")
    fun testGetFollowing() = runTest {
        val xweb = XWebFactory.instance(
            authToken = System.getenv("XWEB_AUTH_TOKEN") ?: "",
            csrfToken = System.getenv("XWEB_CSRF_TOKEN") ?: "",
        )

        val request = FollowingRequest().also {
            it.userId = "44196397"
            it.count = 5
        }

        val response = xweb.user().getFollowing(request)

        println("=== Following ===")
        println("Total: ${response.data.users.size}")

        response.data.users.forEach { user ->
            println("@${user.screenName} (${user.name})")
        }

        assertTrue(response.data.users.isNotEmpty())
    }
}
