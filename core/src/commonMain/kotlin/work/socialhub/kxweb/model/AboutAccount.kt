package work.socialhub.kxweb.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
data class AboutAccount(
    var id: String? = null,
    var createdAt: String? = null,
    var location: String? = null,
    var description: String? = null,
)
