package work.socialhub.kxweb.api

import work.socialhub.kxweb.entity.explore.GetNewsRequest
import work.socialhub.kxweb.entity.explore.GetNewsResponse
import work.socialhub.kxweb.entity.share.Response
import kotlin.js.JsExport

@JsExport
interface ExploreResource {

    suspend fun getNews(request: GetNewsRequest): Response<GetNewsResponse>

    @JsExport.Ignore
    fun getNewsBlocking(request: GetNewsRequest): Response<GetNewsResponse>
}
