package work.socialhub.kxweb

import work.socialhub.kxweb.api.AccountResource
import work.socialhub.kxweb.api.BookmarkResource
import work.socialhub.kxweb.api.EngagementResource
import work.socialhub.kxweb.api.ExploreResource
import work.socialhub.kxweb.api.FollowResource
import work.socialhub.kxweb.api.HomeResource
import work.socialhub.kxweb.api.ListResource
import work.socialhub.kxweb.api.MediaResource
import work.socialhub.kxweb.api.PostResource
import work.socialhub.kxweb.api.SearchResource
import work.socialhub.kxweb.api.TimelineResource
import work.socialhub.kxweb.api.TrendResource
import work.socialhub.kxweb.api.TweetResource
import work.socialhub.kxweb.api.UserResource
import kotlin.js.JsExport

@JsExport
interface XWeb {
    fun account(): AccountResource
    fun search(): SearchResource
    fun tweet(): TweetResource
    fun home(): HomeResource
    fun user(): UserResource
    fun engagement(): EngagementResource
    fun post(): PostResource
    fun follow(): FollowResource
    fun bookmark(): BookmarkResource
    fun list(): ListResource
    fun media(): MediaResource
    fun explore(): ExploreResource
    fun timeline(): TimelineResource
    fun trend(): TrendResource
}
