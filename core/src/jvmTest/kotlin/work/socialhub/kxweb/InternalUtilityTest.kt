package work.socialhub.kxweb

import work.socialhub.khttpclient.HttpRequest
import work.socialhub.kxweb.internal.share.InternalUtility
import work.socialhub.kxweb.internal.share.ClientTransactionId
import work.socialhub.kxweb.internal.share.InternalUtility.withClientTransaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
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
    fun testWithClientTransactionOmitsGeneratedIdWithoutPairData() {
        ClientTransactionId.clearCache()
        val config = XWebConfig().apply {
            enableClientTransaction = true
        }

        val request = HttpRequest().withClientTransaction(config)

        assertNull(request.header["x-client-transaction-id"])
    }

    @Test
    fun testWithClientTransactionUsesExplicitId() {
        ClientTransactionId.clearCache()
        val config = XWebConfig().apply {
            clientTransactionId = "explicit-id"
        }

        val request = HttpRequest().withClientTransaction(config)

        assertEquals("explicit-id", request.header["x-client-transaction-id"])
        assertNull(config.clientTransactionId)
    }

    @Test
    fun testWithClientTransactionUsesProvider() {
        ClientTransactionId.clearCache()
        val config = XWebConfig().apply {
            clientTransactionIdProvider = { method, path -> "$method:$path" }
        }

        val request = HttpRequest().withClientTransaction(config)

        assertEquals("GET:", request.header["x-client-transaction-id"])
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
