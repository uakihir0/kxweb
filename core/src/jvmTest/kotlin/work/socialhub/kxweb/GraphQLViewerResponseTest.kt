package work.socialhub.kxweb

import kotlin.test.Test
import kotlin.test.assertEquals
import work.socialhub.kxweb.internal.entity.GraphQLViewerRoot
import work.socialhub.kxweb.internal.share.InternalUtility.fromJson

class GraphQLViewerResponseTest {

    @Test
    fun testViewerUserIdentity() {
        val response = fromJson<GraphQLViewerRoot>(
            """
            {
              "data": {
                "viewer": {
                  "user_results": {
                    "result": {
                      "__typename": "User",
                      "rest_id": "42",
                      "core": {
                        "name": "Test User",
                        "screen_name": "test_user"
                      }
                    }
                  }
                }
              }
            }
            """.trimIndent()
        )

        val user = response.data?.viewer?.userResults?.result
        assertEquals("42", user?.restId)
        assertEquals("Test User", user?.core?.name)
        assertEquals("test_user", user?.core?.screenName)
    }
}
