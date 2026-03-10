package work.socialhub.kxweb.api

import work.socialhub.kxweb.entity.list.GetListsRequest
import work.socialhub.kxweb.entity.list.ListTimelineRequest
import work.socialhub.kxweb.entity.list.ListTimelineResponse
import work.socialhub.kxweb.entity.share.Response
import kotlin.js.JsExport

@JsExport
interface ListResource {

    suspend fun getOwnedLists(request: GetListsRequest): Response<ListTimelineResponse>

    @JsExport.Ignore
    fun getOwnedListsBlocking(request: GetListsRequest): Response<ListTimelineResponse>

    suspend fun getListMemberships(request: GetListsRequest): Response<ListTimelineResponse>

    @JsExport.Ignore
    fun getListMembershipsBlocking(request: GetListsRequest): Response<ListTimelineResponse>

    suspend fun getListTimeline(request: ListTimelineRequest): Response<ListTimelineResponse>

    @JsExport.Ignore
    fun getListTimelineBlocking(request: ListTimelineRequest): Response<ListTimelineResponse>
}
