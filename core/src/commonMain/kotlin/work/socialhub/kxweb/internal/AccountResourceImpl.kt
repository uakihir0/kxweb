package work.socialhub.kxweb.internal

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import work.socialhub.kxweb.XWebConfig
import work.socialhub.kxweb.api.AccountResource
import work.socialhub.kxweb.domain.Service
import work.socialhub.kxweb.entity.account.GetCurrentUserResponse
import work.socialhub.kxweb.entity.share.Response
import work.socialhub.kxweb.internal.share.InternalUtility
import work.socialhub.kxweb.internal.share.InternalUtility.fromJson
import work.socialhub.kxweb.internal.share.InternalUtility.httpRequest
import work.socialhub.kxweb.internal.share.InternalUtility.setTimeouts
import work.socialhub.kxweb.internal.share.InternalUtility.trackResponse
import work.socialhub.kxweb.internal.share.InternalUtility.withCookieHeaders
import work.socialhub.kxweb.util.toBlocking

class AccountResourceImpl(
    private val config: XWebConfig
) : AccountResource {

    override suspend fun getCurrentUser(): Response<GetCurrentUserResponse> {
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
            } catch (e: Exception) {
                if (url == urls.last()) throw InternalUtility.handleError(e as? Exception ?: Exception(e))
            }
        }

        throw InternalUtility.handleError(null, body = "Failed to get current user from all endpoints")
    }

    override fun getCurrentUserBlocking(): Response<GetCurrentUserResponse> =
        toBlocking { getCurrentUser() }
}
