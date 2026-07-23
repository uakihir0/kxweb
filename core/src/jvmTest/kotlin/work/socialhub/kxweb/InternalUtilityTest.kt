package work.socialhub.kxweb

import work.socialhub.kxweb.internal.share.InternalUtility
import work.socialhub.kxweb.internal.share.ClientTransactionId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class InternalUtilityTest {

    @Test
    fun testGenerateClientTransactionId() {
        ClientTransactionId.setPairData(byteArrayOf(1, 2, 3, 4), "animation")
        try {
            assertTrue(InternalUtility.generateClientTransactionId().isNotBlank())
        } finally {
            ClientTransactionId.clearCache()
        }
    }

    @Test
    fun testGenerateClientTransactionIdUnique() {
        ClientTransactionId.setPairData(byteArrayOf(1, 2, 3, 4), "animation")
        try {
            val first = InternalUtility.generateClientTransactionId()
            val second = InternalUtility.generateClientTransactionId()
            assertNotEquals(first, second)
        } finally {
            ClientTransactionId.clearCache()
        }
    }

    @Test
    fun testRestApiUrl() {
        val url = InternalUtility.restApiUrl("/1.1/account/settings.json")
        assertEquals("https://x.com/i/api/1.1/account/settings.json", url)
    }

    @Test
    fun testGraphqlPostBody() {
        val body = InternalUtility.graphqlPostBody("""{"tweet_id":"123"}""", "abc")
        assertEquals("""{"variables":{"tweet_id":"123"},"queryId":"abc"}""", body)
    }

    @Test
    fun testGraphqlPostBodyWithFeatures() {
        val features = mapOf("flag1" to true, "flag2" to false)
        val body = InternalUtility.graphqlPostBodyWithFeatures("""{"x":"y"}""", features, "qid")
        assertTrue(body.contains(""""flag1":true"""))
        assertTrue(body.contains(""""flag2":false"""))
        assertTrue(body.contains(""""queryId":"qid""""))
    }

    @Test
    fun testIsOAuth() {
        val config = XWebConfig()
        assertEquals(false, InternalUtility.isOAuth(config))

        config.oauthToken = "token"
        config.oauthSecret = "secret"
        assertEquals(true, InternalUtility.isOAuth(config))
    }
}
