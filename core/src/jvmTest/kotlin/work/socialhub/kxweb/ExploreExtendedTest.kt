package work.socialhub.kxweb

import kotlinx.coroutines.test.runTest
import work.socialhub.kxweb.entity.explore.ExploreTab
import work.socialhub.kxweb.entity.explore.GetNewsRequest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExploreExtendedTest {

    @Test
    fun testExploreTabValues() {
        assertEquals(5, ExploreTab.entries.size)
        assertEquals("VGltZWxpbmU6DAC2CwABAAAAB2Zvcl95b3UAAA==", ExploreTab.FOR_YOU.timelineId)
        assertEquals("VGltZWxpbmU6DAC2CwABAAAACHRyZW5kaW5nAAA=", ExploreTab.TRENDING.timelineId)
        assertEquals("VGltZWxpbmU6DAC2CwABAAAABG5ld3MAAA==", ExploreTab.NEWS.timelineId)
        assertEquals("VGltZWxpbmU6DAC2CwABAAAABnNwb3J0cwAA", ExploreTab.SPORTS.timelineId)
        assertEquals("VGltZWxpbmU6DAC2CwABAAAADWVudGVydGFpbm1lbnQAAA==", ExploreTab.ENTERTAINMENT.timelineId)
    }

    @Test
    fun testGetNewsRequestWithTab() {
        val request = GetNewsRequest().also {
            it.tab = ExploreTab.NEWS
            it.count = 10
        }
        assertEquals(ExploreTab.NEWS, request.tab)
        assertEquals(10, request.count)
    }

    @Test
    @Ignore("Integration test - requires auth credentials")
    fun testGetNewsWithTab() = runTest {
        val xweb = XWebFactory.instance(
            authToken = System.getenv("XWEB_AUTH_TOKEN") ?: "",
            csrfToken = System.getenv("XWEB_CSRF_TOKEN") ?: "",
        )

        val request = GetNewsRequest().also {
            it.tab = ExploreTab.TRENDING
            it.count = 5
        }

        val response = xweb.explore().getNews(request)

        println("=== Explore (Trending) ===")
        println("Total: ${response.data.tweets.size}")

        response.data.tweets.forEach { tweet ->
            println("${tweet.text?.take(80)}")
        }

        assertTrue(response.data.tweets.isNotEmpty())
    }
}
