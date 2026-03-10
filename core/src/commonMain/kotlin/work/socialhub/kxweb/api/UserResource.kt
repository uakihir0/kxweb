package work.socialhub.kxweb.api

import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.entity.user.FollowingRequest
import work.socialhub.kxweb.entity.user.FollowingResponse
import work.socialhub.kxweb.entity.user.UserByScreenNameRequest
import work.socialhub.kxweb.entity.user.UserTweetsRequest
import work.socialhub.kxweb.entity.user.UserTweetsResponse
import work.socialhub.kxweb.model.User
import kotlin.js.JsExport

@JsExport
interface UserResource {

    suspend fun getUserByScreenName(
        request: UserByScreenNameRequest
    ): Response<User>

    @JsExport.Ignore
    fun getUserByScreenNameBlocking(
        request: UserByScreenNameRequest
    ): Response<User>

    suspend fun getUserTweets(
        request: UserTweetsRequest
    ): Response<UserTweetsResponse>

    @JsExport.Ignore
    fun getUserTweetsBlocking(
        request: UserTweetsRequest
    ): Response<UserTweetsResponse>

    suspend fun getFollowing(
        request: FollowingRequest
    ): Response<FollowingResponse>

    @JsExport.Ignore
    fun getFollowingBlocking(
        request: FollowingRequest
    ): Response<FollowingResponse>

    suspend fun getFollowers(
        request: FollowingRequest
    ): Response<FollowingResponse>

    @JsExport.Ignore
    fun getFollowersBlocking(
        request: FollowingRequest
    ): Response<FollowingResponse>
}
