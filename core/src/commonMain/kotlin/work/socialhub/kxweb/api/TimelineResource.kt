package work.socialhub.kxweb.api

import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.entity.timeline.GetLikesRequest
import work.socialhub.kxweb.entity.timeline.GetLikesResponse
import kotlin.js.JsExport

@JsExport
interface TimelineResource {

    suspend fun getLikes(request: GetLikesRequest): Response<GetLikesResponse>

    @JsExport.Ignore
    fun getLikesBlocking(request: GetLikesRequest): Response<GetLikesResponse>
}
