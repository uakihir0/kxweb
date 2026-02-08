package work.socialhub.kxweb.entity.search

import kotlin.js.JsExport

@JsExport
enum class SearchType(
    val product: String
) {
    LATEST("Latest"),
    TOP("Top"),
    PEOPLE("People"),
    MEDIA("Media"),
}
