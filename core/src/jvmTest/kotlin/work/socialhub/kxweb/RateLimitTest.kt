package work.socialhub.kxweb

import work.socialhub.kxweb.entity.share.RateLimit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RateLimitTest {

    @Test
    fun testFromHeaders() {
        val headers = mapOf(
            "x-rate-limit-limit" to listOf("100"),
            "x-rate-limit-remaining" to listOf("50"),
            "x-rate-limit-reset" to listOf("1700000000"),
        )

        val rateLimit = RateLimit.fromHeaders(headers)
        assertNotNull(rateLimit)
        assertEquals(100, rateLimit.limit)
        assertEquals(50, rateLimit.remaining)
        assertEquals(1700000000L, rateLimit.resetEpochSeconds)
    }

    @Test
    fun testFromHeadersMissingFields() {
        val headers = mapOf(
            "x-rate-limit-limit" to listOf("100"),
        )

        val rateLimit = RateLimit.fromHeaders(headers)
        assertNull(rateLimit)
    }

    @Test
    fun testFromHeadersEmpty() {
        val rateLimit = RateLimit.fromHeaders(emptyMap())
        assertNull(rateLimit)
    }

    @Test
    fun testIsLimitedWhenExhausted() {
        val rateLimit = RateLimit(
            limit = 100,
            remaining = 0,
            resetEpochSeconds = kotlin.time.Clock.System.now().epochSeconds + 300,
        )
        assertTrue(rateLimit.isLimited())
    }

    @Test
    fun testIsNotLimitedWhenHasRemaining() {
        val rateLimit = RateLimit(
            limit = 100,
            remaining = 50,
            resetEpochSeconds = kotlin.time.Clock.System.now().epochSeconds + 300,
        )
        assertFalse(rateLimit.isLimited())
    }

    @Test
    fun testIsNotLimitedAfterReset() {
        val rateLimit = RateLimit(
            limit = 100,
            remaining = 0,
            resetEpochSeconds = kotlin.time.Clock.System.now().epochSeconds - 10,
        )
        assertFalse(rateLimit.isLimited())
    }

    @Test
    fun testSecondsUntilReset() {
        val future = kotlin.time.Clock.System.now().epochSeconds + 120
        val rateLimit = RateLimit(100, 0, future)
        assertTrue(rateLimit.secondsUntilReset() > 0)
        assertTrue(rateLimit.secondsUntilReset() <= 120)
    }

    @Test
    fun testSecondsUntilResetPast() {
        val past = kotlin.time.Clock.System.now().epochSeconds - 120
        val rateLimit = RateLimit(100, 0, past)
        assertEquals(0L, rateLimit.secondsUntilReset())
    }
}
