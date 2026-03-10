package work.socialhub.kxweb.api

import work.socialhub.kxweb.entity.home.HomeTimelineRequest
import work.socialhub.kxweb.entity.home.HomeTimelineResponse
import work.socialhub.kxweb.entity.share.Response
import kotlin.js.JsExport

@JsExport
interface HomeResource {

    suspend fun getHomeTimeline(
        request: HomeTimelineRequest
    ): Response<HomeTimelineResponse>

    @JsExport.Ignore
    fun getHomeTimelineBlocking(
        request: HomeTimelineRequest
    ): Response<HomeTimelineResponse>

    suspend fun getHomeLatestTimeline(
        request: HomeTimelineRequest
    ): Response<HomeTimelineResponse>

    @JsExport.Ignore
    fun getHomeLatestTimelineBlocking(
        request: HomeTimelineRequest
    ): Response<HomeTimelineResponse>
}
