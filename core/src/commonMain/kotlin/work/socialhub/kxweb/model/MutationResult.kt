package work.socialhub.kxweb.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
data class MutationResult(
    var success: Boolean = false,
    var error: String? = null,
)
