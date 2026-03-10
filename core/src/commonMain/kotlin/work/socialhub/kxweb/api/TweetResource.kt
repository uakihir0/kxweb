package work.socialhub.kxweb.api

import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.entity.tweet.TweetDetailRequest
import work.socialhub.kxweb.entity.tweet.TweetDetailResponse
import work.socialhub.kxweb.model.Tweet
import kotlin.js.JsExport

@JsExport
interface TweetResource {

    suspend fun getTweet(
        tweetId: String
    ): Response<Tweet>

    @JsExport.Ignore
    fun getTweetBlocking(
        tweetId: String
    ): Response<Tweet>

    suspend fun getTweetDetail(
        request: TweetDetailRequest
    ): Response<TweetDetailResponse>

    @JsExport.Ignore
    fun getTweetDetailBlocking(
        request: TweetDetailRequest
    ): Response<TweetDetailResponse>
}
