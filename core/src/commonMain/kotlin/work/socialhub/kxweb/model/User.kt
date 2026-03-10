package work.socialhub.kxweb.model

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
data class User(
    var id: String? = null,
    var screenName: String? = null,
    var name: String? = null,
    var description: String? = null,
    var profileImageUrl: String? = null,
    var profileBannerUrl: String? = null,
    var followersCount: Int? = null,
    var followingCount: Int? = null,
    var statusesCount: Int? = null,
    var listedCount: Int? = null,
    var verified: Boolean? = null,
    var createdAt: String? = null,
    var location: String? = null,
    var url: String? = null,
)
