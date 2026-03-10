package work.socialhub.kxweb.entity.media

import kotlin.js.JsExport

@JsExport
class UploadMediaRequest {
    var data: ByteArray? = null
    var mimeType: String? = null
    var altText: String? = null
}
