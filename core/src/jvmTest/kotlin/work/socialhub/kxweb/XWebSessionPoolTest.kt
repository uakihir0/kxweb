package work.socialhub.kxweb

import work.socialhub.kxweb.entity.share.RateLimit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class XWebSessionPoolTest {

    private fun createPool(): XWebSessionPool {
        return XWebSessionPool(
            listOf(
                XWebSession.cookie("auth1", "csrf1", label = "user1"),
                XWebSession.cookie("auth2", "csrf2", label = "user2"),
                XWebSession.oauth("tok1", "sec1", label = "oauth1"),
            )
        )
    }

    @Test
    fun testPoolSize() {
        val pool = createPool()
        assertEquals(3, pool.size)
        assertEquals(3, pool.availableCount)
    }

    @Test
    fun testAcquireSession() {
        val pool = createPool()
        val session = pool.acquireSession("SearchTimeline")
        assertNotNull(session)
    }

    @Test
    fun testAcquireSessionFromEmptyPool() {
        val pool = XWebSessionPool(emptyList())
        val session = pool.acquireSession()
        assertNull(session)
    }

    @Test
    fun testRateLimitTracking() {
        val pool = createPool()
        val session = pool.acquireSession("SearchTimeline")!!

        // Update with rate limit that's almost exhausted
        val rateLimit = RateLimit(
            limit = 100,
            remaining = 1,
            resetEpochSeconds = kotlinx.datetime.Clock.System.now().epochSeconds + 300,
        )
        pool.updateRateLimit(session, "SearchTimeline", rateLimit)

        // Session should be rate-limited for this endpoint
        assertFalse(pool.isSessionAvailable(session, "SearchTimeline"))

        // But available for other endpoints
        assertTrue(pool.isSessionAvailable(session, "TweetDetail"))
    }

    @Test
    fun testRateLimitSelectsAlternateSession() {
        val pool = createPool()
        val session1 = pool.acquireSession("SearchTimeline")!!

        // Rate-limit session1 for SearchTimeline
        val rateLimit = RateLimit(
            limit = 100,
            remaining = 0,
            resetEpochSeconds = kotlinx.datetime.Clock.System.now().epochSeconds + 300,
        )
        pool.updateRateLimit(session1, "SearchTimeline", rateLimit)

        // Pool should select a different session
        val session2 = pool.acquireSession("SearchTimeline")!!
        assertTrue(session1 != session2)
    }

    @Test
    fun testRateLimitPrefersHigherRemaining() {
        val pool = createPool()
        val sessions = (1..3).map { pool.acquireSession("test")!! }.distinct()

        // Give different remaining counts
        val now = kotlinx.datetime.Clock.System.now().epochSeconds + 300
        pool.updateRateLimit(sessions[0], "test", RateLimit(100, 10, now))
        pool.updateRateLimit(sessions[1], "test", RateLimit(100, 90, now))
        if (sessions.size > 2) {
            pool.updateRateLimit(sessions[2], "test", RateLimit(100, 50, now))
        }

        // Should prefer the session with most remaining
        val selected = pool.acquireSession("test")!!
        val selectedLimit = pool.getRateLimit(selected, "test")
        // The selected should be the one with 90 remaining (highest)
        assertNotNull(selectedLimit)
        assertEquals(90, selectedLimit.remaining)
    }

    @Test
    fun testSessionInvalidation() {
        val pool = createPool()
        assertEquals(3, pool.availableCount)

        val session = pool.acquireSession()!!
        pool.invalidateSession(session)

        assertEquals(2, pool.availableCount)
        assertFalse(pool.isSessionAvailable(session))
    }

    @Test
    fun testGlobalLimiting() {
        val pool = createPool()
        val session = pool.acquireSession()!!

        pool.markGloballyLimited(session)

        assertFalse(pool.isSessionAvailable(session, "SearchTimeline"))
        assertFalse(pool.isSessionAvailable(session, "TweetDetail"))

        // Other sessions should still be available
        val other = pool.acquireSession("SearchTimeline")
        assertNotNull(other)
        assertTrue(other != session)
    }

    @Test
    fun testAddSession() {
        val pool = XWebSessionPool(emptyList())
        assertEquals(0, pool.size)

        pool.addSession(XWebSession.cookie("auth1", "csrf1"))
        assertEquals(1, pool.size)

        val session = pool.acquireSession()
        assertNotNull(session)
        assertIs<XWebSession.Cookie>(session)
    }

    @Test
    fun testRemoveSession() {
        val pool = createPool()
        assertEquals(3, pool.size)

        val session = pool.acquireSession()!!
        pool.removeSession(session)

        assertEquals(2, pool.size)
    }

    @Test
    fun testConfigResolveSession() {
        val pool = createPool()
        val config = XWebConfig()
        config.sessionPool = pool

        // Before resolution, no session-specific credentials
        assertNull(config.currentSession)

        // Resolve session
        config.resolveSession("SearchTimeline")

        // Should have resolved to a session
        assertNotNull(config.currentSession)

        // Credentials should be set
        val session = config.currentSession!!
        when (session) {
            is XWebSession.Cookie -> {
                assertEquals(session.authToken, config.authToken)
                assertEquals(session.csrfToken, config.csrfToken)
                assertNull(config.oauthToken)
            }
            is XWebSession.OAuth -> {
                assertEquals(session.oauthToken, config.oauthToken)
                assertEquals(session.oauthSecret, config.oauthSecret)
                assertNull(config.authToken)
            }
        }
    }

    @Test
    fun testFactoryPooled() {
        val xweb = XWebFactory.instancePooled(
            listOf(
                XWebSession.cookie("auth1", "csrf1"),
                XWebSession.oauth("tok1", "sec1"),
            )
        )
        assertNotNull(xweb)
        assertNotNull(xweb.search())
    }

    @Test
    fun testFactoryPooledFromJsonLines() {
        val jsonl = """
            {"oauth_token":"tok1","oauth_token_secret":"sec1"}
            {"auth_token":"at1","ct0":"ct1"}
        """.trimIndent()

        val xweb = XWebFactory.instancePooledFromJsonLines(jsonl)
        assertNotNull(xweb)
    }

    @Test
    fun testAllSessionsLimitedReturnsSoonestReset() {
        val pool = XWebSessionPool(
            listOf(
                XWebSession.cookie("auth1", "csrf1"),
                XWebSession.cookie("auth2", "csrf2"),
            )
        )

        val now = kotlinx.datetime.Clock.System.now().epochSeconds

        // Rate-limit both sessions with different reset times
        val session1 = XWebSession.cookie("auth1", "csrf1")
        val session2 = XWebSession.cookie("auth2", "csrf2")

        pool.updateRateLimit(session1, "test", RateLimit(100, 0, now + 600))
        pool.updateRateLimit(session2, "test", RateLimit(100, 0, now + 60))

        // Should still return a session (the one with soonest reset)
        val selected = pool.acquireSession("test")
        assertNotNull(selected)
    }
}
