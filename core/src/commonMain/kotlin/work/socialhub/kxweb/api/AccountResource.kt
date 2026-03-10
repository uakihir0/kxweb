package work.socialhub.kxweb.api

import work.socialhub.kxweb.entity.account.GetCurrentUserResponse
import work.socialhub.kxweb.entity.share.Response
import kotlin.js.JsExport

@JsExport
interface AccountResource {

    suspend fun getCurrentUser(): Response<GetCurrentUserResponse>

    @JsExport.Ignore
    fun getCurrentUserBlocking(): Response<GetCurrentUserResponse>
}
