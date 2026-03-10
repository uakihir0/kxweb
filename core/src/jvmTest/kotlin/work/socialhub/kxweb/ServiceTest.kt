package work.socialhub.kxweb

import work.socialhub.kxweb.domain.Service
import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceTest {

    @Test
    fun testServiceConstants() {
        assertEquals("https://x.com/i/api/graphql", Service.X_API_GRAPHQL.uri)
        assertEquals("https://api.x.com/graphql", Service.X_API_GRAPHQL_OAUTH.uri)
        assertEquals("https://x.com/i/api", Service.X_REST_API.uri)
        assertEquals("https://api.x.com", Service.X_REST_API_PUBLIC.uri)
        assertEquals("https://upload.twitter.com/1.1/media/upload.json", Service.X_UPLOAD.uri)
    }

    @Test
    fun testServiceEnumSize() {
        assertEquals(5, Service.entries.size)
    }
}
