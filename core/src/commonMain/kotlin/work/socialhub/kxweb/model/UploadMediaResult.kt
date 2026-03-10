package work.socialhub.kxweb.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
data class UploadMediaResult(
    var success: Boolean = false,
    var mediaId: String? = null,
    var error: String? = null,
)
