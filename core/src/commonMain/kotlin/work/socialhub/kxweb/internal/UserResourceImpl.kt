package work.socialhub.kxweb.internal

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import work.socialhub.kxweb.XWebConfig
import work.socialhub.kxweb.api.UserResource
import work.socialhub.kxweb.domain.QueryId
import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.entity.user.AboutAccountResponse
import work.socialhub.kxweb.entity.user.FollowingRequest
import work.socialhub.kxweb.entity.user.FollowingResponse
import work.socialhub.kxweb.entity.user.GetUserAboutAccountRequest
import work.socialhub.kxweb.entity.user.GetUserIdByUsernameRequest
import work.socialhub.kxweb.entity.user.UserByScreenNameRequest
import work.socialhub.kxweb.entity.user.UserTweetsRequest
import work.socialhub.kxweb.entity.user.UserTweetsResponse
import work.socialhub.kxweb.internal.entity.GraphQLAboutAccountRoot
import work.socialhub.kxweb.internal.entity.GraphQLFollowingRoot
import work.socialhub.kxweb.internal.entity.GraphQLUserByScreenNameRoot
import work.socialhub.kxweb.internal.entity.GraphQLUserTweetsRoot
import work.socialhub.kxweb.internal.share.InternalUtility
import work.socialhub.kxweb.internal.share.InternalUtility.followingFeatures
import work.socialhub.kxweb.internal.share.InternalUtility.fromJson
import work.socialhub.kxweb.internal.share.InternalUtility.graphqlUrl
import work.socialhub.kxweb.internal.share.InternalUtility.httpRequest
import work.socialhub.kxweb.internal.share.InternalUtility.setTimeouts
import work.socialhub.kxweb.internal.share.InternalUtility.userByScreenNameFeatures
import work.socialhub.kxweb.internal.share.InternalUtility.userTweetsFeatures
import work.socialhub.kxweb.internal.share.InternalUtility.withAuthHeaders
import work.socialhub.kxweb.internal.share.TweetParser
import work.socialhub.kxweb.model.AboutAccount
import work.socialhub.kxweb.model.User
import work.socialhub.kxweb.util.toBlocking

class UserResourceImpl(
    private val config: XWebConfig
) : UserResource {

    override suspend fun getUserByScreenName(
        request: UserByScreenNameRequest
    ): Response<User> {
        val url = graphqlUrl(config, QueryId.USER_BY_SCREEN_NAME, "UserByScreenName")

        val variables = buildJsonObject {
            request.screenName?.let { put("screen_name", it) }
            put("withSafetyModeUserFields", true)
        }

        val features = buildJsonObject {
            userByScreenNameFeatures().forEach { (k, v) -> put(k, v) }
        }

        val variablesStr = variables.toString()
        val featuresStr = features.toString()
        val queryParams = mapOf("variables" to variablesStr, "features" to featuresStr)

        val httpRequest = httpRequest(config)
            .url(url)
            .setTimeouts(config)
            .query("variables", variablesStr)
            .query("features", featuresStr)
            .withAuthHeaders(config, "GET", url, queryParams)

        val response = httpRequest.get()
        val body = response.stringBody

        if (response.status !in 200..299) {
            throw InternalUtility.handleError(null, response.status, body)
        }

        val root = fromJson<GraphQLUserByScreenNameRoot>(body)
        val userResult = root.data?.user?.result
            ?: throw InternalUtility.handleError(null, body = "User not found")

        return Response(TweetParser.parseUserResult(userResult), body)
    }

    override fun getUserByScreenNameBlocking(
        request: UserByScreenNameRequest
    ): Response<User> = toBlocking { getUserByScreenName(request) }

    override suspend fun getUserIdByUsername(
        request: GetUserIdByUsernameRequest
    ): Response<User> {
        val screenNameReq = UserByScreenNameRequest().also {
            it.screenName = request.username
        }
        return getUserByScreenName(screenNameReq)
    }

    override fun getUserIdByUsernameBlocking(
        request: GetUserIdByUsernameRequest
    ): Response<User> = toBlocking { getUserIdByUsername(request) }

    override suspend fun getUserAboutAccount(
        request: GetUserAboutAccountRequest
    ): Response<AboutAccountResponse> {
        val url = graphqlUrl(config, QueryId.ABOUT_ACCOUNT_QUERY, "AboutAccountQuery")

        val variables = buildJsonObject {
            request.screenName?.let { put("screen_name", it) }
        }

        val variablesStr = variables.toString()
        val queryParams = mapOf("variables" to variablesStr)

        val httpRequest = httpRequest(config)
            .url(url)
            .setTimeouts(config)
            .query("variables", variablesStr)
            .withAuthHeaders(config, "GET", url, queryParams)

        val response = httpRequest.get()
        val body = response.stringBody

        if (response.status !in 200..299) {
            throw InternalUtility.handleError(null, response.status, body)
        }

        val root = fromJson<GraphQLAboutAccountRoot>(body)
        val aboutProfile = root.data?.user_result_by_screen_name?.result?.about_profile

        val aboutAccount = if (aboutProfile != null) {
            AboutAccount(
                id = aboutProfile.id,
                createdAt = aboutProfile.created_at,
                location = aboutProfile.location,
                description = aboutProfile.description,
            )
        } else {
            null
        }

        return Response(AboutAccountResponse(aboutAccount = aboutAccount), body)
    }

    override fun getUserAboutAccountBlocking(
        request: GetUserAboutAccountRequest
    ): Response<AboutAccountResponse> = toBlocking { getUserAboutAccount(request) }

    override suspend fun getUserTweets(
        request: UserTweetsRequest
    ): Response<UserTweetsResponse> {
        val url = graphqlUrl(config, QueryId.USER_TWEETS, "UserTweets")

        val variables = buildJsonObject {
            request.userId?.let { put("userId", it) }
            put("count", request.count)
            request.cursor?.let { put("cursor", it) }
            put("includePromotedContent", true)
            put("withQuickPromoteEligibilityTweetFields", true)
            put("withVoice", true)
            put("withV2Timeline", true)
        }

        val features = buildJsonObject {
            userTweetsFeatures().forEach { (k, v) -> put(k, v) }
        }

        val variablesStr = variables.toString()
        val featuresStr = features.toString()
        val queryParams = mapOf("variables" to variablesStr, "features" to featuresStr)

        val httpRequest = httpRequest(config)
            .url(url)
            .setTimeouts(config)
            .query("variables", variablesStr)
            .query("features", featuresStr)
            .withAuthHeaders(config, "GET", url, queryParams)

        val response = httpRequest.get()
        val body = response.stringBody

        if (response.status !in 200..299) {
            throw InternalUtility.handleError(null, response.status, body)
        }

        val root = fromJson<GraphQLUserTweetsRoot>(body)
        val instructions = root.data?.user?.result?.timeline_v2?.timeline?.instructions ?: emptyList()
        val result = TweetParser.parseTimelineInstructions(instructions)

        return Response(
            UserTweetsResponse(tweets = result.tweets, cursor = result.cursor),
            body,
        )
    }

    override fun getUserTweetsBlocking(
        request: UserTweetsRequest
    ): Response<UserTweetsResponse> = toBlocking { getUserTweets(request) }

    override suspend fun getFollowing(
        request: FollowingRequest
    ): Response<FollowingResponse> {
        return fetchSocialGraph(request, QueryId.FOLLOWING, "Following")
    }

    override fun getFollowingBlocking(
        request: FollowingRequest
    ): Response<FollowingResponse> = toBlocking { getFollowing(request) }

    override suspend fun getFollowers(
        request: FollowingRequest
    ): Response<FollowingResponse> {
        return fetchSocialGraph(request, QueryId.FOLLOWERS, "Followers")
    }

    override fun getFollowersBlocking(
        request: FollowingRequest
    ): Response<FollowingResponse> = toBlocking { getFollowers(request) }

    private suspend fun fetchSocialGraph(
        request: FollowingRequest,
        queryId: String,
        operationName: String,
    ): Response<FollowingResponse> {
        val url = graphqlUrl(config, queryId, operationName)

        val variables = buildJsonObject {
            request.userId?.let { put("userId", it) }
            put("count", request.count)
            request.cursor?.let { put("cursor", it) }
            put("includePromotedContent", false)
        }

        val features = buildJsonObject {
            followingFeatures().forEach { (k, v) -> put(k, v) }
        }

        val variablesStr = variables.toString()
        val featuresStr = features.toString()
        val queryParams = mapOf("variables" to variablesStr, "features" to featuresStr)

        val httpRequest = httpRequest(config)
            .url(url)
            .setTimeouts(config)
            .query("variables", variablesStr)
            .query("features", featuresStr)
            .withAuthHeaders(config, "GET", url, queryParams)

        val response = httpRequest.get()
        val body = response.stringBody

        if (response.status !in 200..299) {
            throw InternalUtility.handleError(null, response.status, body)
        }

        val root = fromJson<GraphQLFollowingRoot>(body)
        val instructions = root.data?.user?.result?.timeline?.timeline?.instructions ?: emptyList()
        val result = TweetParser.parseUserTimelineInstructions(instructions)

        return Response(
            FollowingResponse(users = result.users, cursor = result.cursor),
            body,
        )
    }
}
