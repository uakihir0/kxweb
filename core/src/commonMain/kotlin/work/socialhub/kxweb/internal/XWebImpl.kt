package work.socialhub.kxweb.internal

import work.socialhub.kxweb.XWeb
import work.socialhub.kxweb.XWebConfig
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
import work.socialhub.kxweb.api.TweetResource
import work.socialhub.kxweb.api.UserResource

class XWebImpl(
    config: XWebConfig
) : XWeb {

    private val account: AccountResource = AccountResourceImpl(config)
    private val search: SearchResource = SearchResourceImpl(config)
    private val tweet: TweetResource = TweetResourceImpl(config)
    private val home: HomeResource = HomeResourceImpl(config)
    private val user: UserResource = UserResourceImpl(config)
    private val engagement: EngagementResource = EngagementResourceImpl(config)
    private val post: PostResource = PostResourceImpl(config)
    private val follow: FollowResource = FollowResourceImpl(config)
    private val bookmark: BookmarkResource = BookmarkResourceImpl(config)
    private val list: ListResource = ListResourceImpl(config)
    private val media: MediaResource = MediaResourceImpl(config)
    private val explore: ExploreResource = ExploreResourceImpl(config)
    private val timeline: TimelineResource = TimelineResourceImpl(config)

    override fun account() = account
    override fun search() = search
    override fun tweet() = tweet
    override fun home() = home
    override fun user() = user
    override fun engagement() = engagement
    override fun post() = post
    override fun follow() = follow
    override fun bookmark() = bookmark
    override fun list() = list
    override fun media() = media
    override fun explore() = explore
    override fun timeline() = timeline
}
