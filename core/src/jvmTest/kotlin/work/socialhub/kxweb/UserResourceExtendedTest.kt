package work.socialhub.kxweb

import kotlinx.coroutines.test.runTest
import work.socialhub.kxweb.entity.user.GetUserAboutAccountRequest
import work.socialhub.kxweb.entity.user.GetUserIdByUsernameRequest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertNotNull

class UserResourceExtendedTest {

    @Test
    @Ignore("Integration test - requires auth credentials")
    fun testGetUserIdByUsername() = runTest {
        val xweb = XWebFactory.instance(
            authToken = System.getenv("XWEB_AUTH_TOKEN") ?: "",
            csrfToken = System.getenv("XWEB_CSRF_TOKEN") ?: "",
        )

        val request = GetUserIdByUsernameRequest().also {
            it.username = "elikiAr0"
        }

        val response = xweb.user().getUserIdByUsername(request)
        val user = response.data

        println("=== User by Username ===")
        println("ID: ${user.id}")
        println("Screen Name: @${user.screenName}")
        println("Name: ${user.name}")

        assertNotNull(user.id)
        assertNotNull(user.screenName)
    }

    @Test
    @Ignore("Integration test - requires auth credentials")
    fun testGetUserAboutAccount() = runTest {
        val xweb = XWebFactory.instance(
            authToken = System.getenv("XWEB_AUTH_TOKEN") ?: "",
            csrfToken = System.getenv("XWEB_CSRF_TOKEN") ?: "",
        )

        val request = GetUserAboutAccountRequest().also {
            it.screenName = "elikiAr0"
        }

        val response = xweb.user().getUserAboutAccount(request)
        val about = response.data.aboutAccount

        println("=== About Account ===")
        println("ID: ${about?.id}")
        println("Created At: ${about?.createdAt}")
        println("Location: ${about?.location}")
        println("Description: ${about?.description}")
    }

    @Test
    fun testGetUserIdByUsernameRequestFields() {
        val request = GetUserIdByUsernameRequest()
        request.username = "test"
        assert(request.username == "test")
    }

    @Test
    fun testGetUserAboutAccountRequestFields() {
        val request = GetUserAboutAccountRequest()
        request.screenName = "test"
        assert(request.screenName == "test")
    }
}
