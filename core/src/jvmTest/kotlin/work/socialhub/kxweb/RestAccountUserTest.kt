package work.socialhub.kxweb

import kotlin.test.Test
import kotlin.test.assertEquals
import work.socialhub.kxweb.internal.entity.RestAccountUser
import work.socialhub.kxweb.internal.share.InternalUtility.fromJson

class RestAccountUserTest {

    @Test
    fun testParseVerifyCredentialsUser() {
        val response = fromJson<RestAccountUser>(
            """
            {
              "id": 42,
              "id_str": "42",
              "screen_name": "test_user",
              "name": "Test User",
              "description": "Profile bio",
              "profile_image_url_https": "https://example.com/avatar.jpg",
              "profile_banner_url": "https://example.com/banner.jpg",
              "followers_count": 20,
              "friends_count": 10,
              "statuses_count": 30,
              "listed_count": 5,
              "verified": true,
              "created_at": "Tue Mar 01 00:00:00 +0000 2010",
              "location": "Tokyo",
              "url": "https://example.com"
            }
            """.trimIndent()
        )

        val user = response.toUser()

        assertEquals("42", user.id)
        assertEquals("test_user", user.screenName)
        assertEquals("Test User", user.name)
        assertEquals("Profile bio", user.description)
        assertEquals("https://example.com/avatar.jpg", user.profileImageUrl)
        assertEquals("https://example.com/banner.jpg", user.profileBannerUrl)
        assertEquals(20, user.followersCount)
        assertEquals(10, user.followingCount)
        assertEquals(30, user.statusesCount)
        assertEquals(5, user.listedCount)
        assertEquals(true, user.verified)
        assertEquals("Tue Mar 01 00:00:00 +0000 2010", user.createdAt)
        assertEquals("Tokyo", user.location)
        assertEquals("https://example.com", user.url)
    }

    @Test
    fun testUseNumericIdWhenIdStrIsMissing() {
        val response = fromJson<RestAccountUser>("""{"id": 42}""")

        assertEquals("42", response.toUser().id)
    }
}
