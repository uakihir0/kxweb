package work.socialhub.kxweb

import kotlin.test.Test
import kotlin.test.assertEquals
import work.socialhub.kxweb.internal.entity.GraphQLViewerRoot
import work.socialhub.kxweb.internal.share.InternalUtility.fromJson
import work.socialhub.kxweb.internal.share.TweetParser

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
                        "created_at": "Tue Mar 01 00:00:00 +0000 2010",
                        "name": "Test User",
                        "screen_name": "test_user"
                      },
                      "avatar": {
                        "image_url": "https://example.com/avatar.jpg"
                      },
                      "legacy": {
                        "followers_count": 20,
                        "friends_count": 10,
                        "profile_banner_url": "https://example.com/banner.jpg",
                        "statuses_count": 30
                      },
                      "location": {
                        "location": "Tokyo"
                      },
                      "profile_bio": {
                        "description": "Profile bio"
                      },
                      "verification": {
                        "verified": true
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

        val parsedUser = TweetParser.parseUserResult(requireNotNull(user))
        assertEquals("Profile bio", parsedUser.description)
        assertEquals("https://example.com/avatar.jpg", parsedUser.profileImageUrl)
        assertEquals("https://example.com/banner.jpg", parsedUser.profileBannerUrl)
        assertEquals(20, parsedUser.followersCount)
        assertEquals(10, parsedUser.followingCount)
        assertEquals(30, parsedUser.statusesCount)
        assertEquals(true, parsedUser.verified)
        assertEquals("Tue Mar 01 00:00:00 +0000 2010", parsedUser.createdAt)
        assertEquals("Tokyo", parsedUser.location)
    }
}
