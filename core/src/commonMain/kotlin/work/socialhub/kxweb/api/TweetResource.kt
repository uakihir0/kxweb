package work.socialhub.kxweb.api

import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.model.Tweet
import kotlin.js.JsExport

@JsExport
interface TweetResource {

    /**
     * Get a single tweet by its ID.
     * Uses X (Twitter) GraphQL TweetResultByRestId endpoint.
     * This endpoint works with Bearer token only (no user auth required).
     */
    suspend fun getTweet(
        tweetId: String
    ): Response<Tweet>

    @JsExport.Ignore
    fun getTweetBlocking(
        tweetId: String
    ): Response<Tweet>
}
