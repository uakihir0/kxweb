package work.socialhub.kxweb.api

import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.entity.tweet.GetRepliesRequest
import work.socialhub.kxweb.entity.tweet.GetRepliesResponse
import work.socialhub.kxweb.entity.tweet.GetThreadRequest
import work.socialhub.kxweb.entity.tweet.GetThreadResponse
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

    suspend fun getReplies(
        request: GetRepliesRequest
    ): Response<GetRepliesResponse>

    @JsExport.Ignore
    fun getRepliesBlocking(
        request: GetRepliesRequest
    ): Response<GetRepliesResponse>

    suspend fun getThread(
        request: GetThreadRequest
    ): Response<GetThreadResponse>

    @JsExport.Ignore
    fun getThreadBlocking(
        request: GetThreadRequest
    ): Response<GetThreadResponse>
}
