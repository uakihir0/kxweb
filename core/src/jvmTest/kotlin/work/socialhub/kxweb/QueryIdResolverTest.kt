package work.socialhub.kxweb

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import work.socialhub.kxweb.internal.share.InternalUtility
import work.socialhub.kxweb.internal.share.QueryIdResolver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class QueryIdResolverTest {

    @Test
    fun testExtractQueryIds() {
        val jsContent = """
            e.exports={queryId:"abc123",operationName:"SearchTimeline",metadata:{featureSwitches:[]}}
            ,{queryId:"def456",operationName:"UserByScreenName",metadata:{featureSwitches:[]}}
            ,{queryId:"ghi789",operationName:"TweetDetail",metadata:{}}
        """.trimIndent()

        val result = QueryIdResolver.extractQueryIds(jsContent)

        assertEquals(3, result.size)
        assertEquals("abc123", result["SearchTimeline"])
        assertEquals("def456", result["UserByScreenName"])
        assertEquals("ghi789", result["TweetDetail"])
    }

    @Test
    fun testExtractQueryIdsEmpty() {
        val result = QueryIdResolver.extractQueryIds("var x = 1;")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testExtractScriptUrls() {
        val html = """
            <html>
            <script src="https://abs.twimg.com/responsive-web/client-web/main.abc123.js"></script>
            <script src="https://abs.twimg.com/responsive-web/client-web/vendors~main.def456.js"></script>
            <script src="https://other.domain.com/bundle.js"></script>
            </html>
        """.trimIndent()

        val urls = QueryIdResolver.extractScriptUrls(html)

        assertEquals(2, urls.size)
        assertTrue(urls.all { it.startsWith("https://abs.twimg.com/responsive-web/client-web") })
    }

    @Test
    fun testExtractScriptUrlsEmpty() {
        val urls = QueryIdResolver.extractScriptUrls("<html><body>Hello</body></html>")
        assertTrue(urls.isEmpty())
    }

    @Test
    fun testInvalidateCache() {
        QueryIdResolver.invalidateCache()
        // Should not throw
    }

    @Test
    fun testCreateRequestAppliesConfiguredTimeouts() {
        val config = XWebConfig().apply {
            requestTimeoutMillis = 101L
            connectTimeoutMillis = 202L
            socketTimeoutMillis = 303L
        }

        val request = QueryIdResolver.createRequest(config)

        assertEquals(101L, request.requestTimeoutMillis)
        assertEquals(202L, request.connectTimeoutMillis)
        assertEquals(303L, request.socketTimeoutMillis)
    }

    @Test
    fun testWithQueryIdRetryUsesCachedIdFirst() = runTest {
        QueryIdResolver.setCachedIds(mapOf("SearchTimeline" to "cached-id"))
        val attemptedIds = mutableListOf<String>()

        try {
            val result = InternalUtility.withQueryIdRetry(
                operationName = "SearchTimeline",
                queryId = "fallback-id",
            ) { queryId ->
                attemptedIds.add(queryId)
                "success"
            }

            assertEquals("success", result)
            assertEquals(listOf("cached-id"), attemptedIds)
        } finally {
            QueryIdResolver.invalidateCache()
        }
    }

    @Test
    fun testResolvePreservesCancellation() = runTest {
        QueryIdResolver.invalidateCache()

        assertFailsWith<CancellationException> {
            QueryIdResolver.resolveWithRefresh(
                operationName = "SearchTimeline",
                fallback = "fallback-id",
            ) {
                throw CancellationException("cancelled")
            }
        }
    }

    @Test
    fun testWithQueryIdRetryCanSkipRefreshOnNotFound() = runTest {
        QueryIdResolver.invalidateCache()
        val attemptedIds = mutableListOf<String>()

        val exception = assertFailsWith<XWebException> {
            InternalUtility.withQueryIdRetry(
                operationName = "SearchTimeline",
                queryId = "fallback-id",
                refreshOnNotFound = false,
            ) { queryId ->
                attemptedIds.add(queryId)
                throw XWebException("not found", null, status = 404)
            }
        }

        assertEquals(404, exception.status)
        assertEquals(listOf("fallback-id"), attemptedIds)
    }
}
