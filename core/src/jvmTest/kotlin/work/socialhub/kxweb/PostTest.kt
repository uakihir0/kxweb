package work.socialhub.kxweb

import kotlinx.coroutines.test.runTest
import work.socialhub.kxweb.entity.post.CreateTweetRequest
import work.socialhub.kxweb.entity.post.DeleteTweetRequest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertNotNull

class PostTest {

    @Test
    @Ignore("Integration test - requires auth credentials, creates real tweet")
    fun testCreateAndDeleteTweet() = runTest {
        val xweb = XWebFactory.instance(
            authToken = System.getenv("XWEB_AUTH_TOKEN") ?: "",
            csrfToken = System.getenv("XWEB_CSRF_TOKEN") ?: "",
        )

        val createRequest = CreateTweetRequest().also {
            it.text = "Test tweet from kxweb SDK ${System.currentTimeMillis()}"
        }

        val createResponse = xweb.post().createTweet(createRequest)
        val tweet = createResponse.data

        println("=== Created Tweet ===")
        println("ID: ${tweet.id}")
        println("Text: ${tweet.text}")

        assertNotNull(tweet.id)

        // Clean up
        val deleteRequest = DeleteTweetRequest().also {
            it.tweetId = tweet.id
        }
        val deleteResponse = xweb.post().deleteTweet(deleteRequest)
        println("Deleted: ${deleteResponse.data.success}")
    }
}
