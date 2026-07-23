package work.socialhub.kxweb

import work.socialhub.khttpclient.HttpRequest
import work.socialhub.kxweb.internal.share.ClientTransactionId
import work.socialhub.kxweb.internal.share.InternalUtility.withCookieHeaders
import work.socialhub.kxweb.internal.share.InternalUtility.withGuestHeaders
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ClientTransactionIdTest {

    @Test
    fun testGenerateFailsWithoutPairData() {
        ClientTransactionId.clearCache()
        assertFailsWith<IllegalStateException> {
            ClientTransactionId.generate()
        }
    }

    @Test
    fun testGenerateWithPairData() {
        // Set mock pair data
        val keyBytes = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        ClientTransactionId.setPairData(keyBytes, "test-animation-key")

        val id = ClientTransactionId.generate("GET", "/i/api/graphql/abc/SearchTimeline")
        assertTrue(id.isNotEmpty())
        // Crypto-based ID should be different from simple
        // (it's base64 encoded so may contain +, /, =)

        ClientTransactionId.clearCache()
    }

    @Test
    fun testGenerateWithPairDataDifferentPaths() {
        val keyBytes = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        ClientTransactionId.setPairData(keyBytes, "test-animation-key")

        val id1 = ClientTransactionId.generate("GET", "/path1")
        val id2 = ClientTransactionId.generate("GET", "/path2")

        // Different paths should produce different IDs
        assertNotEquals(id1, id2)

        ClientTransactionId.clearCache()
    }

    @Test
    fun testGenerateWithPairDataDifferentMethods() {
        val keyBytes = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        ClientTransactionId.setPairData(keyBytes, "test-animation-key")

        val id1 = ClientTransactionId.generate("GET", "/path")
        val id2 = ClientTransactionId.generate("POST", "/path")

        assertNotEquals(id1, id2)

        ClientTransactionId.clearCache()
    }

    @Test
    fun testIsPairDataAvailable() {
        ClientTransactionId.clearCache()
        assertFalse(ClientTransactionId.isPairDataAvailable())

        ClientTransactionId.setPairData(byteArrayOf(0x01), "key")
        assertTrue(ClientTransactionId.isPairDataAvailable())

        ClientTransactionId.clearCache()
        assertFalse(ClientTransactionId.isPairDataAvailable())
    }

    @Test
    fun testCreateRequestUsesConfiguredTimeouts() {
        val config = XWebConfig().apply {
            requestTimeoutMillis = 101L
            connectTimeoutMillis = 202L
            socketTimeoutMillis = 303L
        }

        val request = ClientTransactionId.createRequest(config)

        assertEquals(101L, request.requestTimeoutMillis)
        assertEquals(202L, request.connectTimeoutMillis)
        assertEquals(303L, request.socketTimeoutMillis)
    }

    @Test
    fun testRequiredGuestTransactionHeader() {
        ClientTransactionId.setPairData(
            byteArrayOf(0x01, 0x02, 0x03, 0x04),
            "test-animation-key",
        )
        val config = XWebConfig()

        try {
            val request = HttpRequest().withGuestHeaders(
                config = config,
                guestToken = "guest-token",
                method = "GET",
                url = "https://x.com/i/api/graphql/abc/SearchTimeline",
                requireClientTransaction = true,
            )

            assertTrue(request.header["x-client-transaction-id"].orEmpty().isNotBlank())
        } finally {
            ClientTransactionId.clearCache()
        }
    }

    @Test
    fun testExplicitTransactionIdIsConsumedOnce() {
        ClientTransactionId.setPairData(
            byteArrayOf(0x01, 0x02, 0x03, 0x04),
            "test-animation-key",
        )
        val config = XWebConfig().apply {
            clientTransactionId = "one-shot-id"
            enableClientTransaction = true
        }

        try {
            val first = HttpRequest().withCookieHeaders(
                config = config,
                method = "GET",
                url = "https://x.com/i/api/graphql/abc/SearchTimeline",
            )
            val second = HttpRequest().withCookieHeaders(
                config = config,
                method = "GET",
                url = "https://x.com/i/api/graphql/def/HomeTimeline",
            )

            assertEquals("one-shot-id", first.header["x-client-transaction-id"])
            assertNull(config.clientTransactionId)
            assertNotEquals("one-shot-id", second.header["x-client-transaction-id"])
        } finally {
            ClientTransactionId.clearCache()
        }
    }

    @Test
    fun testTransactionIdProviderReceivesRequestData() {
        val config = XWebConfig()
        val calls = mutableListOf<Pair<String, String>>()
        config.clientTransactionIdProvider = { method, path ->
            calls.add(method to path)
            "$method:$path"
        }

        val request = HttpRequest().withCookieHeaders(
            config = config,
            method = "POST",
            url = "https://x.com/i/api/graphql/abc/CreateTweet?ignored=true",
        )

        assertEquals(
            "POST:/i/api/graphql/abc/CreateTweet",
            request.header["x-client-transaction-id"],
        )
        assertEquals(
            listOf("POST" to "/i/api/graphql/abc/CreateTweet"),
            calls,
        )
    }
}
