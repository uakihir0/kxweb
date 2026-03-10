package work.socialhub.kxweb.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
data class TwitterList(
    var id: String? = null,
    var name: String? = null,
    var description: String? = null,
    var memberCount: Int? = null,
    var subscriberCount: Int? = null,
    var isPrivate: Boolean? = null,
    var createdAt: String? = null,
    var owner: User? = null,
)
