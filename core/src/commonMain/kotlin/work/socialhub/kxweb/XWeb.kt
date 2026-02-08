package work.socialhub.kxweb

import work.socialhub.kxweb.api.SearchResource
import work.socialhub.kxweb.api.TweetResource
import kotlin.js.JsExport

@JsExport
interface XWeb {
    fun search(): SearchResource
    fun tweet(): TweetResource
}
