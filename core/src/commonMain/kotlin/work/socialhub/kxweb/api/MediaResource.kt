package work.socialhub.kxweb.api

import work.socialhub.kxweb.entity.media.UploadMediaRequest
import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.model.UploadMediaResult
import kotlin.js.JsExport

@JsExport
interface MediaResource {

    suspend fun uploadMedia(request: UploadMediaRequest): Response<UploadMediaResult>

    @JsExport.Ignore
    fun uploadMediaBlocking(request: UploadMediaRequest): Response<UploadMediaResult>
}
