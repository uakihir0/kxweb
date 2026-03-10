package work.socialhub.kxweb.api

import work.socialhub.kxweb.entity.post.CreateTweetRequest
import work.socialhub.kxweb.entity.post.DeleteTweetRequest
import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.model.MutationResult
import work.socialhub.kxweb.model.Tweet
import kotlin.js.JsExport

@JsExport
interface PostResource {

    suspend fun createTweet(request: CreateTweetRequest): Response<Tweet>

    @JsExport.Ignore
    fun createTweetBlocking(request: CreateTweetRequest): Response<Tweet>

    suspend fun deleteTweet(request: DeleteTweetRequest): Response<MutationResult>

    @JsExport.Ignore
    fun deleteTweetBlocking(request: DeleteTweetRequest): Response<MutationResult>
}
