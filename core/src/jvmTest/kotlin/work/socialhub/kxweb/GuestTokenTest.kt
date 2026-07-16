package work.socialhub.kxweb

import kotlinx.coroutines.test.runTest
import work.socialhub.kxweb.entity.user.UserByScreenNameRequest
import work.socialhub.kxweb.internal.share.GuestTokenProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GuestTokenTest {

    @Test
    fun testActivateGuestToken() = runTest {
        // No auth required — activation uses the public Bearer token only.
        GuestTokenProvider.invalidate()
        val token = GuestTokenProvider.activate(XWebConfig())

        println("=== Guest Token ===")
        println("token: $token")

        assertNotNull(token)
        assertTrue(token.isNotBlank())
        assertTrue(token.all { it.isDigit() }, "guest token should be numeric")
    }

    @Test
    fun testGuestTokenCaching() = runTest {
        GuestTokenProvider.invalidate()
        val config = XWebConfig()
        val first = GuestTokenProvider.token(config)
        val second = GuestTokenProvider.token(config)

        // Within the TTL, the cached token is reused.
        assertEquals(first, second)
    }

    @Test
    fun testGuestUserByScreenName() = runTest {
        // Guest mode allows reading public user profiles without an account.
        val xweb = XWebFactory.instanceGuest()

        val request = UserByScreenNameRequest().also { it.screenName = "jack" }
        val response = xweb.user().getUserByScreenName(request)
        val user = response.data

        println("=== Guest UserByScreenName ===")
        println("id: ${user.id}")
        println("screenName: @${user.screenName} (${user.name})")
        println("followers: ${user.followersCount}")

        assertNotNull(user)
        assertEquals("jack", user.screenName)
    }
}
