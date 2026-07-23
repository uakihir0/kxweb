package work.socialhub.kxweb.internal.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import work.socialhub.kxweb.model.User

@Serializable
data class RestAccountUser(
    val id: Long? = null,
    @SerialName("id_str")
    val idStr: String? = null,
    @SerialName("screen_name")
    val screenName: String? = null,
    val name: String? = null,
    val description: String? = null,
    @SerialName("profile_image_url_https")
    val profileImageUrlHttps: String? = null,
    @SerialName("profile_banner_url")
    val profileBannerUrl: String? = null,
    @SerialName("followers_count")
    val followersCount: Int? = null,
    @SerialName("friends_count")
    val friendsCount: Int? = null,
    @SerialName("statuses_count")
    val statusesCount: Int? = null,
    @SerialName("listed_count")
    val listedCount: Int? = null,
    val verified: Boolean? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    val location: String? = null,
    val url: String? = null,
) {
    fun toUser() = User(
        id = idStr ?: id?.toString(),
        screenName = screenName,
        name = name,
        description = description,
        profileImageUrl = profileImageUrlHttps,
        profileBannerUrl = profileBannerUrl,
        followersCount = followersCount,
        followingCount = friendsCount,
        statusesCount = statusesCount,
        listedCount = listedCount,
        verified = verified,
        createdAt = createdAt,
        location = location,
        url = url,
    )
}
