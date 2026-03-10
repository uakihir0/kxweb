package work.socialhub.kxweb

import work.socialhub.kxweb.entity.search.SearchType
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchExtendedTest {

    @Test
    fun testSearchTypeValues() {
        assertEquals(6, SearchType.entries.size)
        assertEquals("Latest", SearchType.LATEST.product)
        assertEquals("Top", SearchType.TOP.product)
        assertEquals("People", SearchType.PEOPLE.product)
        assertEquals("Media", SearchType.MEDIA.product)
        assertEquals("Photos", SearchType.PHOTOS.product)
        assertEquals("Videos", SearchType.VIDEOS.product)
    }
}
