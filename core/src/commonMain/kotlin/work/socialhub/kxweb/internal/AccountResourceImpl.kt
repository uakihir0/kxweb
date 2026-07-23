package work.socialhub.kxweb.internal

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import work.socialhub.kxweb.XWebConfig
import work.socialhub.kxweb.api.AccountResource
import work.socialhub.kxweb.domain.QueryId
import work.socialhub.kxweb.entity.account.GetCurrentUserResponse
import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.internal.entity.GraphQLViewerRoot
import work.socialhub.kxweb.internal.share.InternalUtility
import work.socialhub.kxweb.internal.share.InternalUtility.fromJson
import work.socialhub.kxweb.internal.share.InternalUtility.graphqlUrl
import work.socialhub.kxweb.internal.share.InternalUtility.httpRequest
import work.socialhub.kxweb.internal.share.InternalUtility.setTimeouts
import work.socialhub.kxweb.internal.share.InternalUtility.trackResponse
import work.socialhub.kxweb.internal.share.InternalUtility.withAuthHeaders
import work.socialhub.kxweb.internal.share.TweetParser
import work.socialhub.kxweb.util.toBlocking

class AccountResourceImpl(
    private val config: XWebConfig
) : AccountResource {

    override suspend fun getCurrentUser(): Response<GetCurrentUserResponse> =
        getCurrentUserFromViewer()

    private suspend fun getCurrentUserFromViewer(): Response<GetCurrentUserResponse> {
        val url = graphqlUrl(config, QueryId.VIEWER, "Viewer")
        val variables = buildJsonObject {
            put("withCommunitiesMemberships", false)
        }
        val features = buildJsonObject {
            put("subscriptions_upsells_api_enabled", true)
            put("profile_label_improvements_pcf_label_in_post_enabled", true)
            put("responsive_web_profile_redirect_enabled", true)
            put("rweb_tipjar_consumption_enabled", true)
            put("verified_phone_label_enabled", false)
            put("creator_subscriptions_tweet_preview_api_enabled", true)
            put("responsive_web_graphql_skip_user_profile_image_extensions_enabled", false)
            put("responsive_web_graphql_timeline_navigation_enabled", true)
        }
        val fieldToggles = buildJsonObject {
            put("isDelegate", false)
            put("withPayments", false)
            put("withAuxiliaryUserLabels", true)
        }
        val queryParams = mapOf(
            "variables" to variables.toString(),
            "features" to features.toString(),
            "fieldToggles" to fieldToggles.toString(),
        )

        val response = httpRequest(config)
            .url(url)
            .setTimeouts(config)
            .query("variables", queryParams.getValue("variables"))
            .query("features", queryParams.getValue("features"))
            .query("fieldToggles", queryParams.getValue("fieldToggles"))
            .withAuthHeaders(config, "GET", url, queryParams, endpoint = "Viewer")
            .get()
        trackResponse(config, "Viewer", response)
        val body = response.stringBody

        if (response.status !in 200..299) {
            throw InternalUtility.handleError(null, response.status, body)
        }

        val user = fromJson<GraphQLViewerRoot>(body)
            .data?.viewer?.userResults?.result
            ?: throw InternalUtility.handleError(null, body = "Viewer user not found")
        val parsedUser = TweetParser.parseUserResult(user)
        val screenName = parsedUser.screenName
            ?: throw InternalUtility.handleError(null, body = "Viewer screen name not found")

        return Response(
            GetCurrentUserResponse(
                userId = parsedUser.id,
                screenName = screenName,
                name = parsedUser.name,
                user = parsedUser,
            ),
            body,
        )
    }

    override fun getCurrentUserBlocking(): Response<GetCurrentUserResponse> =
        toBlocking { getCurrentUser() }
}
