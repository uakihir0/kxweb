package work.socialhub.kxweb

import kotlinx.coroutines.test.runTest
import work.socialhub.kxweb.entity.trend.GetTrendsRequest
import kotlin.test.Test
import kotlin.test.assertTrue

class TrendTest {

    @Test
    fun testGetTrendLocations() = runTest {
        // Guest mode — no account required.
        val xweb = XWebFactory.instanceGuest()

        val response = xweb.trend().getTrendLocations()
        val locations = response.data.locations

        println("=== Trend Locations ===")
        println("count: ${locations.size}")
        locations.take(5).forEach {
            println("  - ${it.name} (woeid=${it.woeid}, ${it.country})")
        }

        assertTrue(locations.isNotEmpty())
        assertTrue(locations.any { it.woeid == 1L }, "Worldwide (woeid=1) should be present")
    }

    @Test
    fun testGetTrends() = runTest {
        val xweb = XWebFactory.instanceGuest()

        val request = GetTrendsRequest().also { it.woeid = 1 }
        val response = xweb.trend().getTrends(request)
        val result = response.data

        println("=== Trends (Worldwide) ===")
        println("locationName: ${result.locationName}")
        println("asOf: ${result.asOf}")
        println("count: ${result.trends.size}")
        result.trends.take(10).forEach {
            println("  - ${it.name} (volume=${it.tweetVolume})")
        }

        assertTrue(result.trends.isNotEmpty())
    }
}
