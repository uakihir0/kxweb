package work.socialhub.kxweb.api

import work.socialhub.kxweb.entity.engagement.LikeRequest
import work.socialhub.kxweb.entity.engagement.RetweetRequest
import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.model.MutationResult
import kotlin.js.JsExport

@JsExport
interface EngagementResource {

    suspend fun like(request: LikeRequest): Response<MutationResult>

    @JsExport.Ignore
    fun likeBlocking(request: LikeRequest): Response<MutationResult>

    suspend fun unlike(request: LikeRequest): Response<MutationResult>

    @JsExport.Ignore
    fun unlikeBlocking(request: LikeRequest): Response<MutationResult>

    suspend fun retweet(request: RetweetRequest): Response<MutationResult>

    @JsExport.Ignore
    fun retweetBlocking(request: RetweetRequest): Response<MutationResult>

    suspend fun unretweet(request: RetweetRequest): Response<MutationResult>

    @JsExport.Ignore
    fun unretweetBlocking(request: RetweetRequest): Response<MutationResult>
}
