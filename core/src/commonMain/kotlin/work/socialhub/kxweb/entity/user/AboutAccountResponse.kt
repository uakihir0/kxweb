package work.socialhub.kxweb.entity.user

import work.socialhub.kxweb.model.AboutAccount
import kotlin.js.JsExport

@JsExport
data class AboutAccountResponse(
    var aboutAccount: AboutAccount? = null,
)
