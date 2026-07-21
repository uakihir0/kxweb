package work.socialhub.kxweb.internal

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.jsonPrimitive
import work.socialhub.kxweb.XWebConfig
import work.socialhub.kxweb.api.AccountResource
import work.socialhub.kxweb.domain.QueryId
import work.socialhub.kxweb.domain.Service
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
import work.socialhub.kxweb.internal.share.InternalUtility.withCookieHeaders
import work.socialhub.kxweb.util.toBlocking

class AccountResourceImpl(
    private val config: XWebConfig
) : AccountResource {

    override suspend fun getCurrentUser(): Response<GetCurrentUserResponse> {
        try {
            return getCurrentUserFromViewer()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Older sessions may still support the legacy REST endpoints below.
        }

        val urls = listOf(
            "${Service.X_REST_API.uri}/1.1/account/settings.json",
            "${Service.X_REST_API.uri}/1.1/account/verify_credentials.json",
        )

        for (url in urls) {
            try {
                val request = httpRequest(config)
                    .url(url)
                    .setTimeouts(config)
                    .withCookieHeaders(config)

                val response = request.get()
                trackResponse(config, "AccountSettings", response)
                val body = response.stringBody

                if (response.status == 200) {
                    val json = fromJson<JsonObject>(body)
                    val result = GetCurrentUserResponse(
                        screenName = json["screen_name"]?.jsonPrimitive?.content,
                        userId = json["id_str"]?.jsonPrimitive?.content,
                        name = json["name"]?.jsonPrimitive?.content,
                    )
                    return Response(result, body)
                }

                if (response.status in listOf(401, 403)) {
                    throw InternalUtility.handleError(null, response.status, body)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (url == urls.last()) throw InternalUtility.handleError(e)
            }
        }

        throw InternalUtility.handleError(null, body = "Failed to get current user from all endpoints")
    }

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
        val screenName = user.core?.screenName ?: user.legacy?.screenName
            ?: throw InternalUtility.handleError(null, body = "Viewer screen name not found")

        return Response(
            GetCurrentUserResponse(
                userId = user.restId,
                screenName = screenName,
                name = user.core?.name ?: user.legacy?.name,
            ),
            body,
        )
    }

    override fun getCurrentUserBlocking(): Response<GetCurrentUserResponse> =
        toBlocking { getCurrentUser() }
}
