package work.socialhub.kxweb

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CookieStringTest {

    @Test
    fun testInstanceFromCookieString() {
        val cookieString = "auth_token=abc123; ct0=xyz789; _twitter_sess=other_value"
        val xweb = XWebFactory.instanceFromCookieString(cookieString)
        assertNotNull(xweb)
    }

    @Test
    fun testCookieStringParsing() {
        val cookieString = "auth_token=mytoken; ct0=mycsrf; guest_id=v1%3A123"
        val config = XWebConfig().also {
            it.cookieString = cookieString
            it.authToken = "mytoken"
            it.csrfToken = "mycsrf"
        }
        assertEquals("mytoken", config.authToken)
        assertEquals("mycsrf", config.csrfToken)
        assertEquals(cookieString, config.cookieString)
    }

    @Test
    fun testCookieStringWithMissingValues() {
        val cookieString = "guest_id=v1%3A123; lang=en"
        val xweb = XWebFactory.instanceFromCookieString(cookieString)
        assertNotNull(xweb)
    }

    @Test
    fun testEnableClientTransaction() {
        val config = XWebConfig()
        assertEquals(false, config.enableClientTransaction)
        assertNull(config.clientTransactionId)
        config.enableClientTransaction = true
        config.clientTransactionId = "transaction-id"
        assertEquals(true, config.enableClientTransaction)
        assertEquals("transaction-id", config.clientTransactionId)
    }
}
