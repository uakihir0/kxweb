package work.socialhub.kxweb

import kotlinx.coroutines.test.runTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertNotNull

class AccountResourceTest {

    @Test
    @Ignore("Integration test - requires auth credentials")
    fun testGetCurrentUser() = runTest {
        val xweb = XWebFactory.instance(
            authToken = System.getenv("XWEB_AUTH_TOKEN") ?: "",
            csrfToken = System.getenv("XWEB_CSRF_TOKEN") ?: "",
        )

        val response = xweb.account().getCurrentUser()
        val user = response.data

        println("=== Current User ===")
        println("Screen Name: @${user.screenName}")
        println("User ID: ${user.userId}")
        println("Name: ${user.name}")

        assertNotNull(user.screenName)
    }

    @Test
    fun testAccountResourceExists() {
        val xweb = XWebFactory.instance()
        assertNotNull(xweb.account())
    }
}
