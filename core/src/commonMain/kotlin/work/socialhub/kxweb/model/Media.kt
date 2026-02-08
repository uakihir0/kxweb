package work.socialhub.kxweb.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
data class Media(
    var type: String? = null,
    var url: String? = null,
    var width: Int? = null,
    var height: Int? = null,
)
