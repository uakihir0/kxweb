package work.socialhub.kxweb.api

import work.socialhub.kxweb.entity.follow.FollowRequest
import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.model.MutationResult
import kotlin.js.JsExport

@JsExport
interface FollowResource {

    suspend fun follow(request: FollowRequest): Response<MutationResult>

    @JsExport.Ignore
    fun followBlocking(request: FollowRequest): Response<MutationResult>

    suspend fun unfollow(request: FollowRequest): Response<MutationResult>

    @JsExport.Ignore
    fun unfollowBlocking(request: FollowRequest): Response<MutationResult>
}
