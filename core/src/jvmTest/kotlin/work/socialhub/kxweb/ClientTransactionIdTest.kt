package work.socialhub.kxweb

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import work.socialhub.khttpclient.HttpRequest
import work.socialhub.kxweb.internal.share.ClientTransactionId
import work.socialhub.kxweb.internal.share.InternalUtility.withCookieHeaders
import work.socialhub.kxweb.internal.share.InternalUtility.withGuestHeaders
import work.socialhub.kxweb.internal.share.InternalUtility.withPreparedCookieHeaders
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
    fun testRequiredGuestTransactionHeader() = runTest {
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
    fun testExplicitTransactionIdIsConsumedOnce() = runTest {
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
    fun testCookieHeadersOmitGeneratedIdWithoutPairData() {
        ClientTransactionId.clearCache()
        val config = XWebConfig().apply {
            enableClientTransaction = true
        }

        val request = HttpRequest().withCookieHeaders(
            config = config,
            method = "POST",
            url = "https://upload.x.com/i/media/upload.json",
        )

        assertNull(request.header["x-client-transaction-id"])
    }

    @Test
    fun testPreparedCookieHeadersGenerateIdWithPairData() = runTest {
        ClientTransactionId.setPairData(
            byteArrayOf(0x01, 0x02, 0x03, 0x04),
            "test-animation-key",
        )
        val config = XWebConfig().apply {
            enableClientTransaction = true
        }

        try {
            val request = HttpRequest().withPreparedCookieHeaders(
                config = config,
                method = "POST",
                url = "https://upload.x.com/i/media/upload.json",
            )

            assertTrue(request.header["x-client-transaction-id"].orEmpty().isNotBlank())
        } finally {
            ClientTransactionId.clearCache()
        }
    }

    @Test
    fun testRefreshPairDataReportsHttpFailureWithoutCache() = runTest {
        ClientTransactionId.clearCache()

        val exception = assertFailsWith<XWebException> {
            ClientTransactionId.refreshPairData(
                loadHome = {
                    ClientTransactionId.TransactionResponse(
                        status = 503,
                        body = "service unavailable",
                    )
                },
                loadOndemand = {
                    error("ondemand loader should not be called")
                },
            )
        }

        assertEquals(503, exception.status)
        assertEquals("service unavailable", exception.body)
        assertTrue(exception.message.orEmpty().contains("X home page"))
    }

    @Test
    fun testRefreshPairDataKeepsExpiredCachedPairOnFailure() = runTest {
        ClientTransactionId.setPairData(
            keyBytes = byteArrayOf(0x01, 0x02, 0x03, 0x04),
            animationKey = "stale-animation-key",
            cachedAt = 0L,
        )

        try {
            ClientTransactionId.refreshPairData(
                loadHome = {
                    ClientTransactionId.TransactionResponse(
                        status = 503,
                        body = "service unavailable",
                    )
                },
                loadOndemand = {
                    error("ondemand loader should not be called")
                },
            )

            assertTrue(ClientTransactionId.isPairDataAvailable())
            assertTrue(ClientTransactionId.generate("POST", "/upload").isNotBlank())
        } finally {
            ClientTransactionId.clearCache()
        }
    }

    @Test
    fun testExplicitTransactionIdIsConsumedAtomically() = runTest {
        val config = XWebConfig().apply {
            clientTransactionId = "one-shot-id"
        }

        val consumed = (1..20)
            .map { async { config.consumeClientTransactionId() } }
            .awaitAll()
            .filterNotNull()

        assertEquals(listOf("one-shot-id"), consumed)
        assertNull(config.clientTransactionId)
    }

    @Test
    fun testTransactionIdProviderReceivesRequestData() = runTest {
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

    @Test
    fun testForcedGuestTransactionSetupDoesNotSendCookies() {
        val config = XWebConfig().apply {
            guestMode = true
            authToken = "auth-token"
            csrfToken = "csrf-token"
            cookieString = "auth_token=auth-token; ct0=csrf-token"
        }

        val request = ClientTransactionId.createHomeRequest(config)

        assertNull(request.header["cookie"])
    }

    @Test
    fun testCookieTransactionSetupSendsCookies() {
        val config = XWebConfig().apply {
            authToken = "auth-token"
            csrfToken = "csrf-token"
        }

        val request = ClientTransactionId.createHomeRequest(config)

        assertEquals(
            "auth_token=auth-token; ct0=csrf-token",
            request.header["cookie"],
        )
    }
}
